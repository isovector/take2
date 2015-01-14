package controllers

import com.github.nscala_time.time.Imports._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

import models._
import utils._
import utils.DateConversions._
import utils.JSON._

import play.api.db.slick.Config.driver.simple._

object FileMetricsController extends Controller {
  val Table = TableQuery[SnapshotModel]

  private val recentDuration = 5.minutes;

  def getFileLineViews(file: String) = Action {
    import Json._

    val lines = Snapshot.lineviews(_.user) {
      DB.withSession { implicit session =>
        Table
          .where(_.commitId === RepoModel.lastCommit)
          .where(_.file === file)
          .list
      }
    }

    Ok(
      Json.toJson(Map(
        "file" -> toJson(file),
        "commit" -> toJson(Todo.unimplemented),
        "userData" -> toJson(lines.toSeq.map {
          case (user, snaps) => toJson(Map(
            "user" -> toJson(user),
            "timeSpent" -> toJson(lines(user).size),
            "timeSpentByLine" -> toJson(snaps.toSeq.map {
              case (line, count) => Map(
                "line" -> toJson(line.asInstanceOf[Int]),
                "count" -> toJson(count)
              )
            })
          ))
        }
        )
      ))
    ).as("text/text")
  }

  def getUsersInFile(prefix: String) = Action {
    val since = DateTime.now - recentDuration

    // TODO(sandy): it would be nice to get this using getFilesOpenedSince
    val users = DB.withSession { implicit session =>
      Table
        .where(_.commitId === RepoModel.lastCommit)
        .where(_.file.like(prefix + "%"))
        .where(x => x.timestamp > since)
        .list
        }.groupBy(_.user).toSeq.map {
          case (k, v) => k -> v.map(_.file).distinct
        }
        Ok(
          users.mapJs(
            "user" -> (_._1.toJs),
            "files" -> (_._2)
          )
        ).as("text/text")
  }

  def getCurrentlyOpenFiles = Action {
    val files =
      RepoFile.getFilesOpenedSince(DateTime.now - recentDuration) .toSeq.map {
        case (k, v) => k
      }

    Ok(
      Json.toJson(files)
    ).as("text/text")
  }

  def getMostPopularFiles(since: String) = Action {
    val files =
      RepoFile.getFilesOpenedSince(new DateTime(since.toLong))
        .toSeq
        .sortBy(-_._2) // sort by descending viewsigs
        .take(10)

    Ok(
      files.mapJs(
        "file" -> (_._1),
        "snapshots" -> (_._2)
      )
    ).as("text/text")
  }

  def getFileExperts(file: String) = Action {
    val lines = DB.withSession { implicit session =>
      Table
        .where(_.file === file)
        .list
    }.groupBy(_.user)

    val counts = lines.map { case(k, v) =>
      k -> v.length
    }.toSeq

    val totals = (0 /: counts)(_ + _._2)

    Ok(
      counts.mapJs(
        "user" -> (_._1.toJs),
        "views" -> (_._2 / totals)))
  }

  def getFileCoefficients = Action {
    Coefficient.update()

    Ok(
      Coefficient.getAll.sortBy(-_.weight).mapJs(
        "source"      -> (_.src),
        "destination" -> (_.dest),
        "coefficient" -> (_.weight)))
  }

  def getFileCoefficientsFor(file: String) = Action {
    Coefficient.update()

    Ok(
      Coefficient.getAll.filter(_.id._1 == file).sortBy(-_.weight).mapJs(
        "source"      -> (_.src),
        "destination" -> (_.dest),
        "coefficient" -> (_.weight)))
  }
}
