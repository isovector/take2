package models

import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current

case class Commit(
        id: Int,
        branch: String,
        hash: String,
        parent: String) {
    private val Table = TableQuery[CommitModel]

    def save() = {
        DB.withSession { implicit session =>
            Table.filter(_.id === id).update(this)
        }
    }
}

object Commit {
    private val Table = TableQuery[CommitModel]

    def create(_1: String, _2: String, _3: String) = {
      DB.withSession { implicit session =>
        Table += new Commit(0, _1, _2, _3)
      }
    }

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
    def * = (id, branch, hash, parent) <> (commit.tupled, Commit.unapply _)
}

