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
        var id: Option[Int] = None,
        timestamp: DateTime,
        file: String,
        user: User,
        branch: String,
        commit: String,
        lines: Map[Int, Int]) {
    private val Table = TableQuery[SnapshotModel]
    def insert() = {
        // Ensure this Event hasn't already been put into the database
        id match {
            case Some(_) => throw new CloneNotSupportedException
            case None => // do nothing
        }

        DB.withSession { implicit session =>
            id = Some((Table returning Table.map(_.id)) += this)
        }
    }
}

object Snapshot {
    private val Table = TableQuery[SnapshotModel]

    def lineviews[T](grouping: Snapshot => T)(dataset: Seq[Snapshot])
            : Map[T, Map[Int, Int]] = {
        val byKey = dataset.groupBy(grouping)

        // Count lines by key
        byKey.toSeq.map { case (key, snaps) =>
            key -> snaps.flatMap { snap =>
                snap.lines.flatMap { case(line, times) =>
                    // create a length-n array of line numbers
                    Seq.fill(times)(line)
                }
            }.groupBy(line => line).map {
                x => x._1 -> x._2.length
            }
        }.toMap
    }

    // this should probably use an interval too
    def getByUser(user: User): Seq[Snapshot] = {
        DB.withSession { implicit sesion =>
            Table.filter(_.user === user).sortBy(_.timestamp.asc).list
        }
    }


    implicit def implicitMapColumnMapper = MappedColumnType.base[Map[Int, Int], String](
        si =>
            si.toSeq.map{ case (k, v) =>
                k.toString + ":" + v.toString
            }.mkString(","),

        s => s match {
            case "" => Map()
            case s =>
                s.split(",").map { pair =>
                    val tuple = pair.split(":").map(_.toInt)
                    tuple(0) -> tuple(1)
                }.toMap
        }
    )
}

class SnapshotModel(tag: Tag) extends Table[Snapshot](tag, "Snapshot") {
    import Snapshot._

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def timestamp = column[DateTime]("timestamp")
    def file = column[String]("file")
    def user = column[User]("user")
    def branch = column[String]("branch")
    def commit = column[String]("commit")
    def lines = column[Map[Int, Int]]("lines", O.DBType("TEXT"))
    val snapshot = Snapshot.apply _
    def * = (id.?, timestamp, file, user, branch, commit, lines) <> (snapshot.tupled, Snapshot.unapply _)
}

