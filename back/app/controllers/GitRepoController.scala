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
import java.io.File
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import play.api.db.slick.Config.driver.simple._

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

    def retrieveFileByPath(filepath: String) = Action {
        var absPath = RepoModel.local + File.separator + filepath
        var pathLength = (RepoModel.local + File.separator).length

        var fileObj = new File(absPath)

        def getPath(file: File) = file.getPath.substring(pathLength)

        import Json._
        (fileObj.exists match {
            case true => Some(fileObj.isDirectory)
            case false => None
        }) match {
            case Some(true) => 
                Ok(toJson(fileObj.listFiles.filter(!_.isHidden).map(
                file => toJson(Map(
                    "name" -> toJson(file.getName),
                    "path" -> toJson(getPath(file)),
                    "isDir" -> toJson(file.isDirectory)
                ))))).as("text/text")

            case Some(false) => 
                Ok(toJson(Map(
                    "name" -> toJson(fileObj.getName),
                    "path" -> toJson(getPath(fileObj)),
                    "contents" -> toJson(readFile(fileObj))
                ))).as("text/text")

            case None =>
                NotFound
        }
    }

    def initialize = Action.async {
        (repoActor ? RepoManagement.Initialize).mapTo[String].map {
            response =>
            Ok(response)
        }
    }

    def update = Action.async {
        (repoActor ? RepoManagement.Update).mapTo[String].map {
            response =>
            Ok(response)
        }
    }
}

