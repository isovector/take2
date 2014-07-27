package models

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import com.github.nscala_time.time.Imports._

import utils.DateConversions._

case class Snapshot(
        var id: Option[Int] = None,
        timestamp: DateTime,
        file: String,
        user: User,
        commit: String,
        lines: Seq[Int]) {
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
    implicit val implicitSnapshotWrites = new Writes[Snapshot] {
        def writes(snap: Snapshot): JsValue = {
            import User._
            Json.obj(
                "id" -> snap.id.get,
                "timestamp" -> snap.timestamp,
                "file" -> snap.file,
                "user" -> snap.user,
                "commit" -> snap.commit,
                "lines" -> snap.lines
            )
        }
    }

    implicit val implicitSeqColumnMapper = MappedColumnType.base[Seq[Int], String](
        si => si.map(_.toString).mkString(","),
        s => s.split(",").map(_.toInt)
    )
}

class SnapshotModel(tag: Tag) extends Table[Snapshot](tag, "Snapshot") {
    import Snapshot._

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def timestamp = column[DateTime]("timestamp")
    def file = column[String]("file")
    def user = column[User]("user")
    def commit = column[String]("commit")
    def lines = column[Seq[Int]]("lines")
    val snapshot = Snapshot.apply _
    def * = (id.?, timestamp, file, user, commit, lines) <> (snapshot.tupled, Snapshot.unapply _)
}

