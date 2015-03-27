package models

import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current
import utils.Flyweight

case class MemcacheVal(id: String, var value: String) {
  private val Table = TableQuery[MemcacheModel]

  def save() = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).update(this)
    }
  }
}

object Memcache extends Flyweight {
  type T = MemcacheVal
  type Key = String

  private val Table = TableQuery[MemcacheModel]

  protected def insert(v: MemcacheVal): MemcacheVal = {
    DB.withSession {
      implicit session =>
        Table += v
    }
    v
  }

  def create(_1: String, _2: String): MemcacheVal = create(MemcacheVal(_1, _2))

  def get(id: Key): Option[String] = getById(id).map(_.value)
  def apply(id: Key): String = get(id).get
  def +=(tuple: (Key, String)) = {
    val id = tuple._1
    val value = tuple._2

    getById(id).map { memc =>
      memc.value = value
      memc.save()
    }.getOrElse {
     create(id, value)
    }
  }

  def rawGet(id: Key): Option[MemcacheVal] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }
}

class MemcacheModel(tag: Tag) extends Table[MemcacheVal](tag, "Memcache") {
  def id = column[String]("id", O.PrimaryKey)
  def value = column[String]("value")

  val underlying = MemcacheVal.apply _
  def * = (id, value) <> (underlying.tupled, MemcacheVal.unapply _)
}

