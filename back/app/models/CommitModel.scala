package models

import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

case class Commit(
    id: String,
    branch: String,
    parents: Seq[Commit]) {
  private val Table = TableQuery[CommitModel]

  def save() = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).update(this)
    }
  }
}

object Commit extends utils.Flyweight {
  type T = Commit
  type Key = String

  private val Table = TableQuery[CommitModel]

  def create(_1: String, _2: String, _3: Seq[Commit]) = {
    DB.withSession { implicit session =>
      Table += new Commit(_1, _2, _3)
    }

    getById(_1)
  }

  def rawGet(id: String): Option[Commit] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }

  implicit def commitIdToCommit = MappedColumnType.base[Seq[Commit], String](
    c => c.map(_.id).mkString(";"),
    i => i.split(";").filter(_.length != 0).map(id => Commit.getById(id).get))
}

class CommitModel(tag: Tag) extends Table[Commit](tag, "Commit") {
  def id = column[String]("id", O.PrimaryKey)
  def branch = column[String]("branch")
  def parents = column[Seq[Commit]]("parents")

  val commit = Commit.apply _
  def * = (id, branch, parents) <> (commit.tupled, Commit.unapply _)
}

