package models

import play.api._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.Play.current

case class Change(
    id: Int,
    user: User,
    file: String,
    var adds: Int,
    var dels: Int) {
  private val Table = TableQuery[ChangeModel]

  DB.withSession { implicit sesion =>
    Table.filter(_.id === id).update(this)
  }
}

object Change {
  private val Table = TableQuery[ChangeModel]

  def create(_1: User, _2: String, _3: Int, _4: Int) = {
    DB.withSession { implicit session =>
      Table += new Change(0, _1, _2, _3, _4)
    }
  }

  // matt put your shit here
}

class ChangeModel(tag: Tag) extends Table[Change](tag, "Change") {
  def id = column[Int]("id", O.PrimaryKey)
  def user = column[User]("user")
  def file = column[String]("file")
  def adds = column[Int]("adds")
  def dels = column[Int]("dels")

  val change = Change.apply _
  def * = (id, user, file, adds, dels) <> (change.tupled, Change.unapply _)
}

