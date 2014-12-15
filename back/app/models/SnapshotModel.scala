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

import utils.DateConversions._

case class Snapshot(
    id: Int,
    timestamp: DateTime,
    file: String,
    user: User,
    commitId: String,
    symbols: Seq[Symbol]) {
  lazy val commit = Commit.getById(commitId)
}

object Snapshot {
  private val Table = TableQuery[SnapshotModel]

  def create(_1: DateTime, _2: String, _3: User, _4: String, lines: Seq[Int]) = {
    val file = _2

    DB.withSession { implicit session =>
      val symbols =
        TableQuery[SymbolModel]
          .filter(x => x.file === file && (x.line inSetBind lines))
          .list
      Table += new Snapshot(0, _1, _2, _3, _4, symbols)
    }
  }

  def lineviews[T](grouping: Snapshot => T)(dataset: Seq[Snapshot]): Map[T, Map[Int, Int]] = {
    val byKey = dataset.groupBy(grouping)

    // Count lines by key
    byKey.toSeq.map { case (key, snaps) =>
      key ->
        snaps
          .flatMap(_.symbols.map(_.line))
          .groupBy(line => line)
          .map { x =>
            x._1 -> x._2.length
          }
    }.toMap
  }

  // this should probably use an interval too
  def getByUser(user: User): Seq[Snapshot] = {
    DB.withSession { implicit session =>
      Table.filter(_.user === user).sortBy(_.timestamp.asc).list
    }
  }
}

class SnapshotModel(tag: Tag) extends Table[Snapshot](tag, "Snapshot") {
  import Symbol._

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def timestamp = column[DateTime]("timestamp")
  def file = column[String]("file")
  def user = column[User]("user")
  def commitId = column[String]("commitId")
  def symbols = column[Seq[Symbol]]("symbols", O.DBType("TEXT"))

  val snapshot = Snapshot.apply _
  def * = (
    id,
    timestamp,
    file,
    user,
    commitId,
    symbols
  ) <> (snapshot.tupled, Snapshot.unapply _)
}

