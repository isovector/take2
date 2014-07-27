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

import play.api.db.slick.Config.driver.simple._

object SnapshotController extends Controller {
    private var Table = TableQuery[SnapshotModel]

    def list = Action {
        val snaps = DB.withSession { implicit session =>
            Table.list
        }

        Ok(
            Json.toJson(snaps)
        ).as("text/text")
    }

    def delete(id: Int) = Action {
        val drop = DB.withSession { implicit session =>
            Table.where(_.id === id).delete
        }

        Ok
    }

    def create = Action { implicit request =>
        // WHY DOES THIS HAVE TO SUCK SO HARD?
        case class SnapshotFormData(
            id: Option[Int] = None,
            timestamp: Int,
            file: String,
            email: String,
            commit: String,
            lines: Seq[Int]
        )

        val snapFormData = Form(mapping(
            "id" -> optional(number),
            "timestamp" -> number,
            "file" -> text,
            "email" -> text,
            "commit" -> text,
            "lines" -> seq(number)
        )(SnapshotFormData.apply)(SnapshotFormData.unapply)).bindFromRequest.get

        User.getByEmail(snapFormData.email) match {
            case None => User(None, "Anonymous User", snapFormData.email, DateTime.now).insert()
            case Some(user) => {
                user.lastActivity = new DateTime(snapFormData.timestamp)
                user.save()
            }
        }

        val snap = new Snapshot(
          snapFormData.id,
          new DateTime(snapFormData.timestamp),
          snapFormData.file,
          User.getByEmail(snapFormData.email).get,
          snapFormData.commit,
          snapFormData.lines
        );

        if (snap.id.isEmpty) {
            snap.insert()
        } else {
            DB.withSession { implicit session =>
                Table.filter(_.id === snap.id.get).update(snap)
            }
        }

        Ok(
            Json.toJson(snap)
        ).as("text/text")
    }
}

