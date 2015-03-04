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

import actions._
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

  def retrieveFileByPath(filepath: String) = Authenticated { implicit request =>
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

  def initialize = Authenticated { implicit request =>
    RepoModel.initialize
    Ok("cool")
  }

  def commitsLoaded = Authenticated { implicit request =>
    // DEBUG
    Ok(Commit.inMemory.mapJs(
      "id" -> (_.id)
    ))
  }

  def destroyCommits = Authenticated { implicit request =>
    // DEBUG
    DB.withSession { implicit session =>
      TableQuery[CommitModel].delete
    }
    Commit.clear
    Ok("yup")
  }

  def buildSymbols = Authenticated { implicit request =>
    // DEBUG
    DB.withSession { implicit sesion =>
      TableQuery[SymbolModel].delete
    }
    Symbol.synchronizeWithRepo()
    Ok("done")
  }

  def retrieveFileByRegex(toFind: String) = Authenticated { implicit request =>
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

