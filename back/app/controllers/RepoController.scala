package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.Logger

import models._
import utils._
import java.io.File
import scala.concurrent.duration._
import org.joda.time._
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.FileUtils.listFiles
import scala.collection.JavaConversions._


import utils.JSON._

object RepoController extends Controller {
    def readFile(file: File) = {
        val source = scala.io.Source.fromFile(file.getAbsolutePath)
        val lines = source.getLines.mkString("\n")
        source.close()
        lines
    }

    def retrieveFileByPath(filepath: String) = Action {
        var absPath = RepoModel.local + File.separator + filepath
        var pathLength = (RepoModel.local + File.separator).length

        var fileObj = new File(absPath)

        def getPath(file: File) = file.getPath.substring(pathLength)

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
        var dir = new File(RepoModel.local)
        var files = listFiles(
            dir,
            new RegexFileFilter(regex),
            new RegexFileFilter(".*"))
        Ok(
            files.filter(!_.isHidden).mapJs(
                "name" -> (f.getName),
                "path" -> (f.getPath()),
                "isDir" -> (f.isDirectory)
            ).toString
        )
    }
}

