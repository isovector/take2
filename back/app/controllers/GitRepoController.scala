package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.Play.current
import play.api.libs.concurrent.Akka

import models._
import actors._
import utils._
import java.io.File
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import org.apache.commons.io.filefilter.RegexFileFilter
import org.apache.commons.io.FileUtils.listFiles
import scala.concurrent.duration._
import scala.collection.JavaConversions._

import play.api.db.slick.Config.driver.simple._

import utils.JSON._

object GitRepoController extends Controller {
    implicit val implFutureTimeout = Timeout(600 seconds)

    val repoActor = Akka.system.actorOf(
        Props[RepoManagementActor],
        name = "repoMgr"
    )

    implicit val system = Akka.system.dispatcher


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
                        "lastUpdated" -> (x => RepoFile.getByFile(x.getName).getOrElse(
                            RepoFile("","", new com.github.nscala_time.time.Imports.DateTime(0))
                        ).lastUpdated)
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

    def initialize = Action.async {
        (repoActor ? RepoManagement.Initialize).mapTo[String].map {
            response =>
            Ok(response)
        }
    }

    def update(): Unit = {
        repoActor ! RepoManagement.Update
    }
}

