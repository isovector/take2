package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.Play.current
import com.github.nscala_time.time.Imports._

import models._
import java.io.File

import play.api.db.slick.Config.driver.simple._

object GitRepoController extends Controller {
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

        def getName(file: File) = file.getAbsolutePath.substring(pathLength)

        import Json._
        Ok(
            fileObj.isDirectory match {
                case true => toJson(fileObj.listFiles.filter(!_.isHidden).map(
                file => toJson(Map(
                    "name" -> toJson(getName(file)),
                    "isDir" -> toJson(file.isDirectory)
                ))))

                case false => toJson(Map(
                    "name" -> toJson(getName(fileObj)),
                    "contents" -> toJson(readFile(fileObj))
                ))
            }
        ).as("text/text")
    }
}

