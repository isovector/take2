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
    val repo = new FileRepository(RepoModel.local + File.separator + ".git")
    val git = new Git(repo)

    def initialize = {
        // TODO(sandy): figure out how to make this compile
        //git.clone.setURI(RepoModel.remote).setDirectory(RepoModel.local).call
    }

    def update = {
        // TODO(sandy): this can crash with CheckoutConflictException for some reason
        git.pull.call
        git.log.all.call.filter(x => Commit.getByHash(x.getName).isEmpty).map { commit =>
            Logger.info("committing " + commit.getName)
            Commit(
                None, 
                commit.getName,
                commit.getParentCount match {
                    case 0 => ""
                    case _ => commit.getParent(0).getName
                }
            ).insert()

            commit.getParentCount match {
                case 0 => addInitialCommitRecords(commit)
                case _ => updateFileCommitRecords(commit)
            }
        }
    }

    private def updateFileCommitRecords(commit: RevCommit) = {
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
            commit.getName, 
            timestamp
        )
    }

    private def addInitialCommitRecords(commit: RevCommit) = {
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
            commit.getName,
            timestamp
        )
    }
}

