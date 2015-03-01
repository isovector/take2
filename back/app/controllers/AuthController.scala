package controllers

import play.api._
import play.api.mvc._

object AuthController extends Controller {
  def login = Action {
    Ok(views.html.login())
  }

  def authenticate = Action {
    Ok(views.html.login())
//    val callback_uri = routes.AuthController.callback(provider).absoluteURL()
//
//    if (!request.user.isReal) {
//      Redirect(ServiceProvider.beeminder.getAuthURI(callback_uri))
//    } else {
//      Redirect(routes.Application.index.absoluteURL())
//    }
  }

  def logout = Action { implicit request =>
    Ok(views.html.login())
//    session().clear()
//    Redirect(routes.Application.login())
  }

//  object static class Login {
//    public email : String
//    public password : String
//  }
}
