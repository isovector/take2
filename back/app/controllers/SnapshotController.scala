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

  // scalastyle:ignore method.length
  def create = Action { implicit request =>
    case class SnapshotFormData(
      timestamp: Long,
      file: String,
      name: String,
      email: String,
      branch: String,
      commit: String,
      lines: Seq[Int]
    )

    val maybeSnapFormData = Form(mapping(
      "timestamp" -> longNumber,
      "file" -> text,
      "name" -> text,
      "email" -> text,
      "branch" -> text,
      "commit" -> text,
      "lines" -> seq(number)
    )(SnapshotFormData.apply)(SnapshotFormData.unapply)).bindFromRequest

    maybeSnapFormData.fold(_ => BadRequest, { snapFormData =>
      if (!RepoModel.getFile(snapFormData.file).exists) {
        Logger.info("doesn't exist")
        NotFound
      } else if (!RepoFile.isTracked(snapFormData.file)) {
        Logger.info("not tracked")
        NotFound
      } else {
        // Build a user if one doesn't exist
        val user = User.getOrCreate(snapFormData.name, snapFormData.email)
        user.lastActivity = new DateTime(snapFormData.timestamp)
        user.save()

        val when = new DateTime(snapFormData.timestamp)
        val cluster = Cluster.getByUserAndTime(user, when)

        // Build a commit if one doesn't exist
        Commit.getById(snapFormData.commit).getOrElse {
          Logger.info(snapFormData.branch + ": needs update!")
          RepoModel.update(snapFormData.branch)
        }

        val snapshot =
          Snapshot.create(
            when,
            snapFormData.file,
            user,
            snapFormData.commit,
            snapFormData.lines)

        cluster.snapshots = cluster.snapshots :+ snapshot
        cluster.addFile(snapFormData.file)
        cluster.save()

        Ok
      }
    })
  }


}

