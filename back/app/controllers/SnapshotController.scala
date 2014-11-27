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

import play.api.db.slick.Config.driver.simple._

object SnapshotController extends Controller {
    private var Table = TableQuery[SnapshotModel]

    def delete(id: Int) = Action {
        val drop = DB.withSession { implicit session =>
            Table.where(_.id === id).delete
        }

        Ok
    }

    def create = Action { implicit request =>
        // WHY DOES THIS HAVE TO SUCK SO HARD?
        case class SnapshotFormData(
            timestamp: Long,
            file: String,
            name: String,
            email: String,
            branch: String,
            commit: String,
            lines: Seq[Int]
        )

        val snapFormData = Form(mapping(
            "timestamp" -> longNumber,
            "file" -> text,
            "name" -> text,
            "email" -> text,
            "branch" -> text,
            "commit" -> text,
            "lines" -> seq(number)
        )(SnapshotFormData.apply)(SnapshotFormData.unapply)).bindFromRequest.get

        if (!RepoModel.getFile(snapFormData.file).exists) {
            // is there a nicer way of doing this?
            Logger.info("doesn't exist")
            NotFound
        } else {
            // Build a user if one doesn't exist
            User.getByEmail(snapFormData.email).map { user =>
                user.lastActivity = new DateTime(snapFormData.timestamp)
                user.save()
            }.getOrElse {
                User.create(
                    snapFormData.name,
                    snapFormData.email,
                    new DateTime(snapFormData.timestamp))
            }

            // Build a commit if one doesn't exist
            Commit.getByHash(snapFormData.commit).getOrElse {
                Logger.info(snapFormData.branch + ": needs update!")
                RepoModel.update(snapFormData.branch)
            }

            Snapshot.create(
              new DateTime(snapFormData.timestamp),
              snapFormData.file,
              User.getByEmail(snapFormData.email).get,
              snapFormData.branch,
              snapFormData.commit,
              snapFormData.lines.map(_ -> 1).toMap);

            Ok
        }
    }
}

