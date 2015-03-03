package controllers

import play.api._
import play.api.mvc._

import actions._

object Application extends Controller {

  def index = Authenticated { implicit request =>
    Ok(views.html.index())
  }
}
