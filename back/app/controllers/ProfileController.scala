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

object ProfileController extends Controller {

  def getUser(userId: Int) = Action {
	val user = User.getById(userId);
	user match{ 
		case Some(u: User) =>
			Ok(
				views.html.profile(u.asJs(
				"id" -> (_.id),
				"name" -> (_.name),
				"email" -> (_.email),
				"expertise" -> (_.getExpertise())
			).toString
	)) 
		case None =>
			// error
			Forbidden
	}
  }
}
