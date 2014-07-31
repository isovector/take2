package models

import java.io.File
import play.api._
import scala.collection.JavaConversions._
import org.eclipse.jgit._
import org.eclipse.jgit.api._
import org.eclipse.jgit.storage._
import org.eclipse.jgit.storage.file._
import org.gitective.core._

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
        git.log.all.call.filter(x => Commit.getByHash(x.getName).isEmpty).map { diff =>
            Logger.info("committing " + diff.getName)
            Commit(
                None, 
                diff.getName,
                diff.getParentCount match {
                    case 0 => ""
                    case _ => diff.getParent(0).getName
                }
            ).insert()
        }
    }
}

