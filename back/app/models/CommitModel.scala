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
    author: User,
    branch: String,
    rawParents: Seq[String]) {
  lazy val parents = rawParents.map(x => Commit.getById(x).get)

  def children = Commit.inMemory.filter(_.parents.map(_.id).contains(id))
  def isHead = RepoModel.isHead(this)
}

object Commit extends utils.Flyweight {
  type T = Commit
  type Key = String

  private val Table = TableQuery[CommitModel]

  def create(_1: String, _2: User, _3: String, _4: Seq[Commit]) = {
    DB.withSession { implicit session =>
      Table += Commit(_1, _2, _3, _4.map(_.id))
    }

    getById(_1).get
  }

  def rawGet(id: String): Option[Commit] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }

  implicit def commitIdToCommit = MappedColumnType.base[Seq[String], String](
    ss => ss.mkString(";"),
    s=> s.split(";").filter(_.length != 0))
}

class CommitModel(tag: Tag) extends Table[Commit](tag, "Commit") {
  import Commit._

  def id = column[String]("id", O.PrimaryKey)
  def author = column[User]("author")
  def branch = column[String]("branch")
  def rawParents = column[Seq[String]]("rawParents", O.DBType("TEXT"))

  val commit = Commit.apply _
  def * = (id, author, branch, rawParents) <> (commit.tupled, Commit.unapply _)
}

