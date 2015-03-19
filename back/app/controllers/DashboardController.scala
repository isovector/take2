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

object DashboardController extends Controller {
  val Table = TableQuery[SnapshotModel]

  private val recentDuration = 5.minutes;

  def getCurrentlyOpenFiles = Action {
    val files =
      RepoFile.getFilesOpenedSince(DateTime.now - recentDuration) .toSeq.map {
        case (k, v) => k
      }

    Ok(
      Json.toJson(files)
    ).as("text/text")
  }

  def getMostActiveUsers(since: String) = Action {
    val users = DashboardModel.getMostActiveUsers(new DateTime(since.toLong))

    Ok(
      users.mapJs(
        "user" -> (_._1.toJs),
        "snapshots" -> (_._2)
      )
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

  def getFileCoefficients = Action {
    Coefficient.update()

    Ok(
      Coefficient.getAll.sortBy(-_.weight).mapJs(
        "source"      -> (_.src),
        "destination" -> (_.dest),
        "coefficient" -> (_.weight)))
  }

  def getAbsoluteExperts = Action {
    val experts = DashboardModel.getAbsoluteExperts()

    Ok(
      experts.toSeq.mapJs(
        "user" -> (_._1.toJs),
        "knowledge" -> (_._2)))
  }

  def getClusteredSymbols(id: Int) = Action {
    Symbol.getById(id) match {
      case Some(symbol: Symbol) =>
        Ok(
          Cluster
            .getClusteredSymbols(symbol)
            .toSeq
            .sortBy(-_._2)
            .mapJs(
              "symbol" -> (_._1.asJs(
                "id" -> (_.id),
                "file" -> (_.file),
                "line" -> (_.line),
                "name" -> (_.name),
                "kind" -> (_.kind)
              )),
              "weight" -> (_._2)))

      case None => NotFound
    }
  }

  def getAllUsers = Action {
    Ok(
      User.getAll().mapJs(
        "id" -> (_.id),
        "name" -> (_.name),
        "email" -> (_.email)
      ).toString
    ).as("text/js")
  }
}
