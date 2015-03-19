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

object UsersController extends Controller {

  def getUsers = Action {
  	// Needs to pass a list of all users
	Ok(views.html.users())
  }
}
