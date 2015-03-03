package controllers

import play.api._
import play.api.mvc._

import actions._

object Application extends Controller {

  def index = LoginAware { implicit request =>
    if (request.user == "LOGIN") {
      Redirect(routes.AuthController.authenticate)
    } else {
      Ok(views.html.index())
    }
  }
}
