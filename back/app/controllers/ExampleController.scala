package controllers

import models.{ExampleModel, Dad}
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import scala.slick.driver.SQLiteDriver.simple._


object ExampleController extends Controller {
    // Gets the first dad in the db. There might not be one, hence firstOption rather than first
    val existingDad = DB.withSession { implicit session =>
        TableQuery[ExampleModel].firstOption
    }

    var dad = existingDad.getOrElse(new Dad(name = "Lou"))
  
    // If no dad was found in the db, add the new one
    if (existingDad.isEmpty) {
        DB.withSession { implicit session =>
            TableQuery[ExampleModel] += dad
        }
    }

    // Mapping between the form data and our object
    val userForm = Form(mapping(
        "id" -> optional(number),
        "name" -> text
    )(Dad.apply)(Dad.unapply))
   
    // Referenced in conf/routes as action when page is visited
    def index = Action {
        // Updates form with ExampleModel's (singleton object) current value
        Ok(views.html.example(name = dad.name))
        
        // If id had been passed through
        //Ok(views.html.example(name = dad.name, id = dad.id))
    }

    // Referenced in conf/routes as action when form is submitted
    def update = Action { implicit request =>
        // Parse the form data

        // creates new dad from old id and new name, then updates db
        val updatedDad = userForm.bindFromRequest.get
        dad = new Dad(id = dad.id, updatedDad.name)
        DB.withSession { implicit session =>
            TableQuery[ExampleModel].filter(_.id === dad.id.get).update(dad)
        }

        // If dad.id.get had been passed to the view and then to updatedDad, could have done:
        //dad = userForm.bindFromRequest.get
        //DB.withSession { implicit session =>
        //    TableQuery[ExampleModel].filter(_.id === dad.id.get).update(dad)
        //}
        // Chose not to as it would be a security issue

        
        // Redirect to example page after calculation
        Redirect("/example")
    }

}

