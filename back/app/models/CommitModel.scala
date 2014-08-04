package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._

case class Commit(
        var id: Option[Int] = None,
        branch: String,
        hash: String,
        parent: String) {
    private val Table = TableQuery[CommitModel]
    def insert() = {
        id match {
            case Some(_) => throw new CloneNotSupportedException
            case None => // do nothing
        }

        DB.withSession { implicit session =>
            id = Some((Table returning Table.map(_.id)) += this)
	    }
    }

    def save() = {
        id match {
            case Some(_) => // do nothing
            case None => throw new NullPointerException 
        }

        DB.withSession { implicit session =>
            Table.filter(_.id === id.get).update(this)
        }
    }
}

object Commit {
    private val Table = TableQuery[CommitModel]

    def getByHash(hash: String): Option[Commit] = {
        DB.withSession { implicit session =>
            Table.filter(_.hash === hash).firstOption
        }
    }
}

class CommitModel(tag: Tag) extends Table[Commit](tag, "Commit") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def branch = column[String]("branch")
    def hash = column[String]("name")
    def parent = column[String]("email")

    val commit = Commit.apply _
    def * = (id.?, branch, hash, parent) <> (commit.tupled, Commit.unapply _)
}

