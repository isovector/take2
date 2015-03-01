package models

import com.github.nscala_time.time.Imports._
import com.jcraft.jsch.{Session, JSch}
import com.jcraft.jsch.agentproxy._
import java.io.File
import org.eclipse.jgit._
import org.eclipse.jgit.api._
import org.eclipse.jgit.api.errors._
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.diff._
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.storage._
import org.eclipse.jgit.storage.file._
import org.eclipse.jgit.transport._
import org.eclipse.jgit.treewalk._
import org.eclipse.jgit.util.FS
import org.eclipse.jgit.util.io._
import org.gitective.core._
import play.api._
import scala.collection.JavaConversions._

trait GitModel extends SourceRepositoryModel {
  private val repo = new FileRepository(local + File.separator + ".git")
  private val git = new Git(repo)
  private val revwalk = new RevWalk(repo)

  val defaultBranch = "master"

  RepoFile.parseAccioIgnore()

  def initialize = {
    SshSessionFactory.setInstance(new JschConfigSessionFactory() {
      override protected def configure(host: OpenSshConfig.Host, session: Session): Unit = {
          // This can be removed, but the overriden method is required since JschConfigSessionFactory is abstract
          session.setConfig("StrictHostKeyChecking", "false");
      }
      
      /*override protected def createDefaultJSch(fs: FS): JSch =  {
          Connector con = null;
          try {
              if(SSHAgentConnector.isConnectorAvailable()){
                  //USocketFactory usf = new JUnixDomainSocketFactory();
                  USocketFactory usf = new JNAUSocketFactory();
                  con = new SSHAgentConnector(usf);
              }
          } catch(AgentProxyException e){
              System.out.println(e);
          }
   
          if (con == null) {
              return super.createDefaultJSch(fs)
          } else {
              val jsch = new JSch();
              jsch.setConfig("PreferredAuthentications", "publickey");
              val irepo = new RemoteIdentityRepository(con);
              jsch.setIdentityRepository(irepo);
              knownHosts(jsch, fs) // private method from parent class, yeah for Groovy!
              return jsch
          }
      }*/
    })
  
    if (!getFile("").exists()) {
      Git.cloneRepository.setURI(remote).setDirectory(getFile("")).call
    }

    try {
      update("master")
    } catch {
      case _: RefNotFoundException => throw new Exception("The repository was not initialized")
    }
  }

  def update(branch: String) = {
    setBranch(branch)

    git.pull.call

    RepoFile.parseAccioIgnore()

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

    Symbol.synchronizeWithRepo()
  }

  def lastCommit = repo.resolve(Constants.HEAD).name

  def isHead(commit: Commit): Boolean = {
    utils.Todo.hack
    // HACK(sandy): this probably should rely on commit.children.length == 0

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

