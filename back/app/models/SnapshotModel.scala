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
    lines: Map[Int, Int],
    dirty: Boolean, /* does this snap need to be propagated still? */
    symbols: Seq[Symbol]) {
  lazy val commit = Commit.getById(commitId)
}

object Snapshot {
  private val Table = TableQuery[SnapshotModel]

  def create(_1: DateTime, _2: String, _3: User, _4: String, _5: Map[Int, Int], _6: Boolean = true) = {
    val file = _2
    val lines = _5.keys

    DB.withSession { implicit session =>
      val symbols =
        TableQuery[SymbolModel]
          .filter(x => x.file === file && (x.line inSetBind lines))
          .list
      Table += new Snapshot(0, _1, _2, _3, _4, _5, _6, symbols)
    }
  }

  def lineviews[T](grouping: Snapshot => T)(dataset: Seq[Snapshot]): Map[T, Map[Int, Int]] = {
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
    DB.withSession { implicit session =>
      Table.filter(_.user === user).sortBy(_.timestamp.asc).list
    }
  }

  def fastforward(commit: Commit): Unit = {
    // This needs to be two steps to avoid doing extra work for diamond patterns
    propagate(commit)
    coalesce(commit)
  }

  // Propagate snapshots from older commits up the tree
  private def propagate(commit: Commit): Unit = {
    val toPropagate = lineviews(c => (c.file, c.user)) {
      DB.withSession { implicit session =>
        Table.filter(c => c.commitId === commit.id && c.dirty).list
      }
    }.toSeq.map { case (k, lines) =>
      k -> JsObject(
        lines.map { case (line, count) =>
          line.toString -> JsNumber(count)
        }.toSeq).toString
    }

    commit.children.map { dstCommit =>
      toPropagate.map { case (k@(filepath, user), json) =>
        import scala.sys.process._
        import java.io.ByteArrayInputStream

        val resultJson = (Seq(
          "accio",
          "translate",
          "--old_commit", commit.id,
          "--new_commit", dstCommit.id,
          "--filename", filepath,
          "--repo_path", RepoModel.local
        ) #< new ByteArrayInputStream(json.getBytes("UTF-8"))).!!

        val resultLines = Json.parse(resultJson).asInstanceOf[JsObject].value

        k -> resultLines.toSeq.map { case (line, count) =>
          line.toInt -> count.as[Int]
        }
      }.map { case ((filepath, user), lines) =>
        Snapshot.create(
          new DateTime(0), // TODO(sandy): get the right time for this
          filepath,
          user,
          dstCommit.id,
          lines.toMap)

        // Propagate next commit
        propagate(dstCommit)
      }
    }
  }

  private def coalesce(commit: Commit): Unit = {
    // Compress all snapshots for a commit into one per user per file
    val snaps = lineviews(c => (c.file, c.user)) {
      DB.withSession { implicit session =>
        Table.filter(c => c.commitId === commit.id).list
      }
    }

    DB.withSession { implicit session =>
      Table
        .filter(c => c.commitId === commit.id)
        .delete

      snaps.map { case((filepath, user), lines) =>
        Snapshot.create(
          new DateTime(0), // TODO(sandy): get the right time for this
          filepath,
          user,
          commit.id,
          lines,
          false)
      }
    }

    commit.children.map { child =>
      if (!child.isHead) {
        coalesce(child)
      }
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
  import Symbol._

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def timestamp = column[DateTime]("timestamp")
  def file = column[String]("file")
  def user = column[User]("user")
  def commitId = column[String]("commitId")
  def lines = column[Map[Int, Int]]("lines", O.DBType("TEXT"))
  def dirty = column[Boolean]("dirty")
  def symbols = column[Seq[Symbol]]("symbols")

  val snapshot = Snapshot.apply _
  def * = (
    id,
    timestamp,
    file,
    user,
    commitId,
    lines,
    dirty,
    symbols
  ) <> (snapshot.tupled, Snapshot.unapply _)
}

