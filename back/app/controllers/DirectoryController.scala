package controllers

import play.api._
import play.api.mvc._

object DirectoryController extends Controller {

  def directory(filename: String) = Action {
    //TODO: GET DATA FROM SERVER
    
    Ok(views.html.directory(filename = filename))
  }
}
