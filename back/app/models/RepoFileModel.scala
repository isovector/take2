package models

import play.api._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.Play.current
import scala.io._

import com.github.nscala_time.time.Imports._

import utils._
import utils.DateConversions._

case class RepoFile(
    file: String,
    var lastCommit: String,
    var lastUpdated: DateTime,
    var adds: Int,
    var dels: Int) {
  private val Table = TableQuery[RepoFileModel]

  val exists = RepoFile.exists(file)

  def save() = {
    DB.withSession { implicit session =>
      Table.filter(_.file === file).update(this)
    }
  }

  def touch(commitId: String, timestamp: DateTime) = {
    if (lastUpdated < timestamp) {
      lastCommit = commitId
      lastUpdated = timestamp
      save()
    }
  }
}

object RepoFile {
  private val Table = TableQuery[RepoFileModel]
  private var ignoreRules = Seq[String]()

  def parseAccioIgnore() = {
    try {
      ignoreRules =
        Source
          .fromFile(RepoModel.getFile(".accioignore"))
          .getLines
          .toList
          .filter(_(0) != '#')
    } catch {
      case _: Throwable =>
        println("can't parse accioignore")
        ignoreRules = Seq()
    }
  }

  private def doesGlobMatch(str: String, pattern: String) = {
    val p =
      pattern
        .replace(".", "\\.")
        .replace("*", ".*")

    str.matches(p)
  }

  def isTracked(file: String): Boolean = {
    !ignoreRules.exists(rule => doesGlobMatch(file, rule))
  }

  def getIgnoreRules = ignoreRules

  def create(_1: String, _2: String, _3: DateTime) = {
    DB.withSession { implicit session =>
      Table += RepoFile(_1, _2, _3, 0, 0)
    }
  }

  def getAll: Map[String, RepoFile] = {
    DB.withSession { implicit session =>
      Table.list
    } .filter(_.exists)
      .map(r => r.file -> (r: RepoFile))
      .toMap
  }

  def getByFile(file: String): Option[RepoFile] = {
    DB.withSession { implicit session =>
      Table.filter(_.file === file).firstOption
    }
  }

  def touchFiles(
      filenames: Seq[String],
      branch: String,
      commitId: String,
      timestamp: DateTime) = {
    filenames.map { filename =>
      getByFile(filename).map { file =>
        if (branch == RepoModel.defaultBranch) {
          file.touch(commitId, timestamp)
        }
      }.getOrElse {
        RepoFile.create(
          filename,
          commitId,
          timestamp)
      }
    }
  }

  def getFilesOpenedSince(since: DateTime): Map[String, Int] = {
    DB.withSession { implicit session =>
      TableQuery[SnapshotModel]
        .where(x => x.timestamp > since)
        .list
    }.groupBy(_.file).map {
      // Only count number of snapshots
      case (k, v) => k -> v.length
    }
  }

  def exists(file: String) = RepoModel.getFile(file).exists
}

class RepoFileModel(tag: Tag) extends Table[RepoFile](tag, "RepoFile") {
  def file = column[String]("file", O.PrimaryKey)
  def lastCommit = column[String]("lastCommit")
  def lastUpdated = column[DateTime]("lastUpdated")
  def adds = column[Int]("adds")
  def dels = column[Int]("dels")
  def fileIndex = index("repo_file_idx", file, unique = false)

  val repoFile = RepoFile.apply _
  def * = (file, lastCommit, lastUpdated, adds, dels) <>
    (repoFile.tupled, RepoFile.unapply _)
}

