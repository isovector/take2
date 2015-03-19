package controllers

import java.io.File
import org.joda.time._
import play.api._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.concurrent.Akka
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import scala.collection.JavaConversions._
import scala.concurrent.duration._

import models._
import utils._
import utils.JSON._


object RepoController extends Controller {
  def readFile(file: File) = {
    val source = scala.io.Source.fromFile(file.getAbsolutePath)
    val lines = source.getLines.mkString("\n")
    source.close()
    lines
  }

  def getPath(file: File) = {
    var pathLength = (RepoModel.local + File.separator).length
    file.getPath.substring(pathLength)
  }

  def retrieveFileByPath(filepath: String) = Action {
    var fileObj = RepoModel.getFile(filepath)

    (fileObj.exists match {
      case true => Some(fileObj.isDirectory)
      case false => None
      }) match {
        case Some(true) =>
          Ok(views.html.directory(
            fileObj.listFiles.filter(!_.isHidden).mapJs(
              "name" -> (_.getName),
              "path" -> (getPath(_)),
              "isDir" -> (_.isDirectory),
              "lastUpdated" -> (x =>
                  RepoFile.getByFile(getPath(x)) match {
                    case Some(file) => file.lastUpdated
                    case None =>
                      new DateTime(0)
                  })
                ).toString
              ))

          case Some(false) =>
            Ok(views.html.file(
              fileObj.asJs(
                "name" -> (_.getName),
                "path" -> (getPath(_)),
                "contents" -> (readFile(_))
              ).toString
            ))

          case None => NotFound
      }
  }

  def initialize = Action {
    RepoModel.initialize
    Ok("cool")
  }

  def retrieveFileByRegex(toFind: String) = Action {
    val regex = ".*?" + toFind + ".*?"
    val files = RepoFile.getAll.filter(_._1.matches(regex)).map(_._2)

    Ok(
      files.toSeq.mapJs(
        "name" -> (_.file),
        "path" -> (_.file)
      ).toString
    )
  }
}

