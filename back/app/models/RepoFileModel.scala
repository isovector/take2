package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._

import com.github.nscala_time.time.Imports._

import utils._
import utils.DateConversions._

case class RepoFile(
        file: String,
        var lastCommit: String,
        var lastUpdated: DateTime) {
    private val Table = TableQuery[RepoFileModel]
    def insert() = {
        // TODO(sandy): check to see if this exists already

        DB.withSession { implicit session =>
            Table += this
	    }
    }

    def save() = {
        DB.withSession { implicit session =>
            Table.filter(_.file === file).update(this)
        }
    }
}

object RepoFile {
    private val Table = TableQuery[RepoFileModel]

    def getByFile(file: String): Option[RepoFile] = {
        DB.withSession { implicit session =>
            Table.filter(_.file === file).firstOption
        }
    }
}

class RepoFileModel(tag: Tag) extends Table[RepoFile](tag, "RepoFile") {
    def file = column[String]("file", O.PrimaryKey)
    def lastCommit = column[String]("lastCommit")
    def lastUpdated = column[DateTime]("lastUpdated")

    val repoFile = RepoFile.apply _
    def * = (file, lastCommit, lastUpdated) <> (repoFile.tupled, RepoFile.unapply _)
}

