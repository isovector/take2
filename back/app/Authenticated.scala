package actions

import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.mvc.Security._
import play.api.Play.current

import models._

object LoginAware extends AuthenticatedBuilder({ request =>
  val email = request.session.get("email").getOrElse("")

  if (email.isEmpty) {
    Some("LOGIN")
  } else if (Global.whitelist contains email) {
    Some(email)
  } else {
    None
  }
})

object Authenticated extends AuthenticatedBuilder({ request =>
  request.session.get("email").flatMap { email =>
    if (Global.whitelist contains email)
      Some(email)
    else
      None
  }
})
