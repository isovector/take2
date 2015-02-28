package models

import com.github.nscala_time.time.Imports._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

case class Coefficient(
    id: (String, String),
    clusterCount: Int,
    totalCount: Int) {
  val src = id._1
  val dest = id._2
  lazy val weight = clusterCount.toFloat / totalCount
}

object Coefficient {
  private val Table = TableQuery[CoefficientModel]

  def set(id: (String, String), clusterCount: Int, totalCount: Int) = {
    val updated = new Coefficient(id, clusterCount, totalCount)

    DB.withSession { implicit session =>
      val existing = Table.filter(_.id === id)

      existing.firstOption.map { _ =>
        existing.update(updated)
      }.getOrElse {
        Table += updated
      }
    }
  }

  def get(id: (String, String)): Coefficient = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }.getOrElse(new Coefficient(id, 0, 0))
  }

  def getAll(): Seq[Coefficient] = {
    DB.withSession { implicit session =>
      Table.list
    }
  }

  def rebuild() = {
    Memcache += "coeffTimestamp" -> "0"
    DB.withSession { implicit session =>
      Table.delete
    }
    update()
  }

  def update() = {
    val slidingWindow = 10
    val clusterTime = Cluster.clusterTime

    val totalInstances = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)
    val clusteredWith = scala.collection.mutable.Map[(String, String), Int]().withDefaultValue(0)

    val after = Memcache.get("coeffTimestamp").map { timestamp =>
      new DateTime(timestamp.toLong)
    }.getOrElse(new DateTime(0))

    Coefficient.getAll().map { coeff =>
      totalInstances(coeff.src) = coeff.totalCount
      clusteredWith(coeff.id) = coeff.clusterCount
    }

    User.getAll().map { user =>
      val snapshots = Snapshot.getByUser(user).filter(_.timestamp >= after)

      snapshots.sliding(slidingWindow).toList.map { window =>
        val before = window.head.timestamp + clusterTime
        val cluster = window.filter(_.timestamp <= before).map(_.file).toSet

        cluster.map { file => totalInstances(file) += 1 }
        cluster.toSeq.combinations(2).toList.map { choose =>
          clusteredWith((choose(0), choose(1))) += 1
          clusteredWith((choose(1), choose(0))) += 1
        }
      }
    }

    Memcache += "coeffTimestamp" -> DateTime.now.getMillis.toString
    clusteredWith.toMap.map { case (id@(src, _), count) =>
      Coefficient.set(id, count, totalInstances(src))
    }
  }

  implicit def implStrTupColMap = MappedColumnType.base[(String, String), String](
    ss => ss._1 + ":" + ss._2,
    s => {
      val split = s.split(":")
      (split(0), split(1))
    })
}

class CoefficientModel(tag: Tag) extends Table[Coefficient](tag, "Coefficient") {
  import Coefficient._

  def id = column[(String, String)]("id", O.PrimaryKey)
  def clusterCount = column[Int]("clusterCount")
  def totalCount = column[Int]("totalCount")

  val commit = Coefficient.apply _
  def * = (id, clusterCount, totalCount) <> (commit.tupled, Coefficient.unapply _)
}

