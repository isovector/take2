package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import java.io.File
import org.joda.time.DateTime

import models._
import utils._

trait SourceRepositoryModel {
    val remote = "git@github.com:Paamayim/take2.git"
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
        val Table = TableQuery[SnapshotModel]

        val touchedFiles = getFilePathsInCommit(srcCommit)
        val linesPerFile = DB.withSession { implicit session =>
            Table
            .where(_.file inSet touchedFiles)
            .where(_.commit === srcCommit)
            .list
        }.groupBy(_.file).map { case(file, snaps) =>
            file -> snaps.flatMap { snap =>
                snap.lines
            }.groupBy(line => line).map {
                x => x._1 -> x._2.length
            }
        }

        linesPerFile.map { case(filepath, lines) =>
            import scala.sys.process._
            filepath -> Seq(
                "accio",
                "--file=" + filepath,
                "--lines=" + lines.toSeq.sortBy(_._1).mkString
            ).!!.split("\n").map { x =>
                // TODO(sandy): make this get the right line numbers
                x.toInt -> 5
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

