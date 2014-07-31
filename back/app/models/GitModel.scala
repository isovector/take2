package models

import java.io.File
import play.api._
import scala.collection.JavaConversions._
import org.eclipse.jgit._
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.diff._
import org.eclipse.jgit.util.io._
import org.eclipse.jgit.api._
import org.eclipse.jgit.storage._
import org.eclipse.jgit.storage.file._
import org.gitective.core._

import com.github.nscala_time.time.Imports._

object GitModel {
    val repo = new FileRepository(RepoModel.local + File.separator + ".git")
    val git = new Git(repo)
    
    def test = {
        git.log.all.call.map {
            diff => Logger.info(diff.getAuthorIdent.getName)
        }
    }

    def update = {
        // TODO(sandy): this breaks if someone commits on top of you
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

            if (commit.getParentCount > 0) {
                getFilesInCommit(commit)
            }
        }
    }

    private def getFilesInCommit(commit: RevCommit) = {
        val parent = commit.getParent(0) 
        val df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repo);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);

        val timestamp = new DateTime(commit.getCommitTime.toLong * 1000)

        df.scan(parent.getTree, commit.getTree).map { diff =>
            val name = diff.getNewPath
            RepoFile.getByFile(name) match {
                case None => {
                    Logger.info("adding " + name + " to " + commit.getName)
                    RepoFile(
                        name,
                        commit.getName,
                        timestamp
                    ).insert()
                }
                case Some(file) => {
                    if (file.lastUpdated < timestamp) {
                        Logger.info("updating " + file + " to " + commit.getName)
                        file.lastCommit = commit.getName
                        file.lastUpdated = timestamp
                        file.save()
                    }
                }
            }
        }
    }
}

