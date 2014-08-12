package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import java.io.File
import org.joda.time.DateTime

import models._
import utils._

trait SourceRepositoryModel {
    def initialize: Unit
    def update(branch: String): Unit
    def getFilePath(localPath: String) =
        RepoModel.local + File.separator + localPath
    def getFile(localPath: String) = new File(getFilePath(localPath))
    def getFilePathsInCommit(hash: String): Seq[String]
}

object RepoModel {
    val remote = "git@github.com:Paamayim/take2.git"
    val local = "repo"

    private val impl = GitModel

    // Forward methods to implementation
    val defaultBranch = impl.defaultBranch
    def initialize = impl.initialize
    def update = impl.update _
    def getFile = impl.getFile _
    def getFilePath = impl.getFilePath _
    def getFilePathsInCommit(hash: String) = impl.getFilePathsInCommit(hash)

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
                dstCommit,
                newlines.toSeq.map(_._1) // TODO(sandy): make this use counts also
            ).insert()
        }
    }
}

