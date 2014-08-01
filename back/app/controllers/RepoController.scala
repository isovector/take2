package controllers

import java.io.File
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.FileUtils.listFiles
import org.joda.time._
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import play.api.libs.concurrent.Akka
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
        var absPath = RepoModel.local + File.separator + filepath
        var fileObj = new File(absPath)

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
                            RepoFile.getByFile(x.getName) match {
                                case Some(file) => file.lastUpdated
                                case None => new DateTime(0)
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

            case None =>
                NotFound
        }
    }

    def initialize = Action {
        RepoModel.initialize
        Ok("cool")

    def retrieveFileByRegex(regex: String) = Action {
        var files = listFiles(
            new File(RepoModel.local),
            new RegexFileFilter(regex),
            new RegexFileFilter(".*"))
        Ok(
            files.filter(!_.isHidden).to[Seq].mapJs(
                "name" -> (_.getName),
                "path" -> (getPath(_)),
            ).toString
        )
    }
}

