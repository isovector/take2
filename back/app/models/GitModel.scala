package models

import java.io.File
import play.api._
import scala.collection.JavaConversions._
import org.eclipse.jgit._
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.treewalk._
import org.eclipse.jgit.diff._
import org.eclipse.jgit.util.io._
import org.eclipse.jgit.api._
import org.eclipse.jgit.storage._
import org.eclipse.jgit.storage.file._
import org.gitective.core._

import com.github.nscala_time.time.Imports._

object GitModel extends SourceRepositoryModel {
    private val repo = new FileRepository(RepoModel.local + File.separator + ".git")
    private val git = new Git(repo)

    val defaultBranch = "master"

    def initialize = {
        Git.cloneRepository.setURI(RepoModel.remote).setDirectory(RepoModel.getFile("")).call
        update("master")
    }

    def update(branch: String) = {
        setBranch(branch)

        git.pull.call
        git.log.add(
            repo.resolve("HEAD")
        ).call.filter(x => Commit.getByHash(x.getName).isEmpty).map { commit =>
            Logger.info("committing " + commit.getName)
            Commit(
                None,
                branch,
                commit.getName,
                commit.getParentCount match {
                    case 0 => ""
                    case _ => commit.getParent(0).getName
                }
            ).insert()

            commit.getParentCount match {
                case 0 => addInitialCommitRecords(commit, branch)
                case _ => updateFileCommitRecords(commit, branch)
            }
        }
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
        }
    }

    private def updateFileCommitRecords(commit: RevCommit, branch: String) = {
        val parent = commit.getParent(0)
        val df = new DiffFormatter(DisabledOutputStream.INSTANCE)
        df.setRepository(repo)
        df.setDiffComparator(RawTextComparator.DEFAULT)
        df.setDetectRenames(true)

        val timestamp = new DateTime(commit.getCommitTime.toLong * 1000)

        RepoFile.touchFiles(
            df.scan(parent.getTree, commit.getTree).map { diff =>
                diff.getNewPath
            },
            branch,
            commit.getName,
            timestamp
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

