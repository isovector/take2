package models

import play.api.db.slick.Config.driver.simple._

// id is option as auto-generated
case class Dad(id: Option[Int] = None, name: String)

/**
 *  Object creates a singleton class
 *  Creases a database table called "Daddy"
 **/
class ExampleModel(tag: Tag) extends Table[Dad](tag, "Dad") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = (id.?, name) <> (Dad.tupled, Dad.unapply _)
}

