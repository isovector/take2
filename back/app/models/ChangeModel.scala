package models

import play.api._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.Play.current

case class Change(id: Int, x: Int) {
}

object Change {
  private val Table = TableQuery[ChangeModel]

  def create() = {
    DB.withSession { implicit session =>
      Table += new Change(0, 0)
    }
  }

  // matt put your shit here
}

class ChangeModel(tag: Tag) extends Table[Change](tag, "Change") {
  def id = column[Int]("id", O.PrimaryKey)
  def x = column[Int]("x", O.PrimaryKey)

  val change = Change.apply _
  def * = (id, x) <> (change.tupled, Change.unapply _)
}

