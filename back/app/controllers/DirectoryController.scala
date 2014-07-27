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

object DirectoryController extends Controller {
  def directory(filename: String) = Action {
      //TODO: GET DATA FROM SERVER
      var absPath = RepoModel.local + File.separator + filename
      var pathLength = (RepoModel.local + File.separator).length

      var fileObj = new File(absPath)
      if (!fileObj.exists) {
          NotFound
      }
      if (fileObj.isDirectory) {
          Ok(views.html.directory(filename = filename))
      } else {
          Ok(views.html.file())
      }
  }
}
