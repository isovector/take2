package models

import com.typesafe.config.ConfigFactory
import java.io.ByteArrayInputStream
import java.io.File
import org.joda.time.DateTime
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Logger
import play.api.Play.current
import scala.io._

import utils._

trait SourceRepositoryModel {
  case class FileChange(file: String, adds: Int, dels: Int)

  private val config = ConfigFactory.load

  val remote = config.getString("accio.remote")
  val local = config.getString("accio.local")
  val defaultBranch: String

  // Prototype methods
  def lastCommit: String
  def initialize: Unit
  def update(branch: String): Unit
  def countLines(commit: Commit): Seq[FileChange]
  def getFilePathsInCommit(hash: String): Seq[String]
  def isHead(commit: Commit): Boolean

  // Implemented methods
  def getFilePath(localPath: String) =
    local + File.separator + localPath
  def getFile(localPath: String) = new File(getFilePath(localPath))

  def updateCounts(commit: Commit): Unit = {
    if (commit.parents.length > 1) return

    countLines(commit)
      .foreach { case FileChange(file, adds, dels) =>
        val repoFile = RepoFile.getByFile(file).get
        repoFile.adds += adds
        repoFile.dels += dels
        repoFile.save()

        val change = Change.getOrCreate(commit.author, file)
        change.adds += adds
        change.dels += dels
        change.save()
      }
  }
}

object RepoModel extends GitModel

