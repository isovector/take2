package models

import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current

case class Coefficient(
        id: Option[String],
        weight: Float) {
    private val split = id.get.split(":")
    val source = split(0)
    val dest = split(1)

    private val Table = TableQuery[CoefficientModel]
}

object Coefficient {
    private val Table = TableQuery[CoefficientModel]
}

class CoefficientModel(tag: Tag) extends Table[Coefficient](tag, "Coefficient") {
    def id = column[String]("id", O.PrimaryKey)
    def weight = column[Float]("weight")

    val commit = Coefficient.apply _
    def * = (id.?, weight) <> (commit.tupled, Coefficient.unapply _)
}

