package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._

import actions._
import models._
import oauth2.ServiceProvider

object AuthController extends Controller {
  def authenticate = Action { implicit request =>
    val callback_uri = routes.AuthController.callback.absoluteURL()

    Redirect(ServiceProvider.facebook.getAuthURI(callback_uri, Seq()))
  }

  // Grant additional permissions to a facebook token
  def obtainPermission = Action { implicit request =>
    val callback_uri = routes.AuthController.callback.absoluteURL()

    Redirect(ServiceProvider.facebook.getAuthURI(callback_uri, Seq("email")))
  }

  private def exchangeFacebookCodeForToken(
      callback_uri: String,
      queryString: Map[String, Seq[String]]): Either[String, String] = {
    queryString.get("code") match {
      case Some(code) => {
        try {
          val token = ServiceProvider.facebook.exchangeCodeForToken(
            code.toList(0), callback_uri).get

          val idPayload = ServiceProvider.facebook.getResource(
            "/me",
            token.token,
            Map("fields" -> "email")
          ).get

          Logger.info(idPayload.toString)

          val email = (idPayload \ "email").as[String]
          Right(email)
        } catch {
          // TODO(sandy): in an ideal world, we would try again
          case e: NoSuchElementException =>
            Left("Unable to connect to Facebook")
        }
      }

      case None => {
        Left("Unable to connect to Facebook")
      }
    }
  }

  def callback = Action { implicit request =>
    exchangeFacebookCodeForToken(
      routes.AuthController.callback.absoluteURL(),
      request.queryString
    ) match {
      // TODO: make this show an error page?
      case Left(error) => InternalServerError(error)
      case Right(email) => Redirect(
        request.session("redirect_to")
      ).withSession(
        session + ("email" -> email) - "redirect_to")
    }
  }

  def logout = Authenticated { implicit request =>
    Redirect(
      routes.Application.index.absoluteURL()
    ).withSession(session - "email")
  }
}

