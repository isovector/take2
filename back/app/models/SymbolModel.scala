package models

import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Logger
import play.api.Play.current

case class Symbol(id: Int, file: String, name: String, var line: Int, kind: String)

object Symbol extends utils.Flyweight {
  type T = Symbol
  type Key = Int

  private val Table = TableQuery[SymbolModel]

  def create(_1: String, _2: String, _3: Int, _4: String) = {
    getById(
      DB.withSession { implicit session =>
        (Table returning Table.map(_.id)) += new Symbol(0, _1, _2, _3, _4)
      }
    )
  }

  def rawGet(id: Key): Option[Symbol] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }
}

class SymbolModel(tag: Tag) extends Table[Symbol](tag, "Symbol") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def file = column[String]("file")
  def name = column[String]("name")
  def line = column[Int]("line")
  def kind = column[String]("kind")

  val underlying = Symbol.apply _
  def * = (
    id,
    file,
    name,
    line,
    kind
  ) <> (underlying.tupled, Symbol.unapply _)
}

