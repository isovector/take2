package models

import play.api.Logger
import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import java.io.ByteArrayInputStream
import java.io.File
import org.joda.time.DateTime

import models._
import utils._

trait SourceRepositoryModel {
    val remote = "git@github.com:Paamayim/take2.git"
    //val remote = "git@github.com:Paamayim/well-contributed.git"
    val local = "repo"
    val defaultBranch: String

    // Prototype methods
    def lastCommit: String
    def initialize: Unit
    def update(branch: String): Unit
    def getFilePathsInCommit(hash: String): Seq[String]

    // Implemented methods
    def getFilePath(localPath: String) =
        local + File.separator + localPath
    def getFile(localPath: String) = new File(getFilePath(localPath))


    def fastforward(srcCommit: String, dstCommit: String) = {
        import play.api.libs.json._

        val Table = TableQuery[SnapshotModel]

        val linesPerFile = Snapshot.lineviews(_.file){
            DB.withSession { implicit session =>
                Table
                .where(_.commit === srcCommit)
                .list
            }
        }.toSeq.map { case (k, lines) =>
            k ->
                JsObject(
                    lines.map { case (line, count) =>
                        line.toString -> JsNumber(count)
                    }.toSeq
                )
        }

        Logger.info("fast forwarding")

        linesPerFile.map { case(filepath, lines) =>
            import scala.sys.process._

            val json = lines.toString
            val is = new ByteArrayInputStream(json.getBytes("UTF-8"))

            Logger.info("for file " + filepath)

            val resultJson = (Seq(
                "accio",
                "translate",
                "--old_commit", srcCommit,
                "--new_commit", dstCommit,
                "--filename", filepath,
                "--repo_path", local
            ) #< is).!!

            val resultLines = Json.parse(resultJson).asInstanceOf[JsObject].value
            filepath -> resultLines.toSeq.map { case (line, count) =>
                line.toInt -> count.as[Int]
            }
        }.map { case(filepath, newlines) =>
            Snapshot(
                None,
                new DateTime(0), // TODO(sandy): get the right time for this
                filepath,
                User.getById(1).get, // TODO(sandy): figure out a proper user for this
                defaultBranch,       // TODO(sandy): is this a meaningful branch?
                dstCommit,
                newlines.toMap
            ).insert()
        }
    }
}

object RepoModel extends GitModel

