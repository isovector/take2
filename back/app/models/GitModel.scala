package models

import java.io.File
import org.eclipse.jgit._
import org.eclipse.jgit.api._
import org.eclipse.jgit.diff._
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.storage._
import org.eclipse.jgit.storage.file._
import org.eclipse.jgit.treewalk._
import org.eclipse.jgit.util.io._
import org.gitective.core._
import play.api._
import scala.collection.JavaConversions._

import com.github.nscala_time.time.Imports._

trait GitModel extends SourceRepositoryModel {
  private val repo = new FileRepository(local + File.separator + ".git")
  private val git = new Git(repo)
  private val revwalk = new RevWalk(repo)

  val defaultBranch = "master"

  def initialize = {
    //Git.cloneRepository.setURI(remote).setDirectory(getFile("")).call
    update("master")
  }

  def update(branch: String) = {
    setBranch(branch)

    val srcCommit = lastCommit

    git.pull.call
    git.log.add(repo.resolve("HEAD")).call.filter(
      x => Commit.getById(x.getName).isEmpty
    ).toList.reverse.map { commit =>
      Logger.info("creating commit " + commit.getName)
      Commit.create(
        commit.getName,
        branch,
        commit.getParents.map(_.getName).map(Commit.getById _).map(_.get)
      )

      commit.getParentCount match {
        case 0 => addInitialCommitRecords(commit, branch)
        case _ => updateFileCommitRecords(commit, branch)
      }
    }

    val dstCommit = lastCommit

    if (srcCommit != dstCommit) {
      Snapshot.propagate(Commit.getById(srcCommit).get)
    }
  }

  def lastCommit = repo.resolve(Constants.HEAD).name

  def isHead(commit: Commit): Boolean = {
    commit.id == lastCommit
  }

  def getFilePathsInCommit(hash: String): Seq[String] =
    getFilePathsInCommit(revwalk.parseCommit(repo.resolve(hash)))

  def getFilePathsInCommit(commit: RevCommit): Seq[String] = {
    val parent = commit.getParent(0)
    val df = new DiffFormatter(DisabledOutputStream.INSTANCE)
    df.setRepository(repo)
    df.setDiffComparator(RawTextComparator.DEFAULT)
    df.setDetectRenames(true)

    df.scan(parent.getTree, commit.getTree).map(_.getNewPath)
  }

  private def setBranch(branch: String) = {
    import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
    import org.eclipse.jgit.api.errors._

    try {
      git.checkout.setName(branch).call
    } catch {
      case _: DetachedHeadException | _: RefNotFoundException =>
        git.checkout
          .setCreateBranch(true)
          .setName(branch)
          .setUpstreamMode(SetupUpstreamMode.TRACK)
          .setStartPoint("origin/" + branch)
          .call

          // TODO(sandy): there is probably a better way to do this
          // but it works so YOLO
          val config = repo.getConfig
          config.setString("branch", branch, "remote", "origin")
          config.setString("branch", branch, "merge", "refs/heads/" + branch)
          config.save

          git.checkout.setName(branch).call
      case _: NullPointerException => Logger.info("failed to set branch")
    }
  }

  private def updateFileCommitRecords(commit: RevCommit, branch: String) = {
    RepoFile.touchFiles(
      getFilePathsInCommit(commit),
      branch,
      commit.getName,
      new DateTime(commit.getCommitTime.toLong * 1000)
    )
  }

  private def addInitialCommitRecords(commit: RevCommit, branch: String) = {
    val walk = new TreeWalk(repo)
    walk.addTree(commit.getTree)
    walk.setRecursive(true)

    val timestamp = new DateTime(commit.getCommitTime.toLong * 1000)

    var files: List[String] = Nil
    while (walk.next) {
      files = files :+ walk.getPathString
    }

    RepoFile.touchFiles(
      files,
      branch,
      commit.getName,
      timestamp
    )
  }
}

