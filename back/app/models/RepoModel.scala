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

import models._
import utils._

trait SourceRepositoryModel {
  private val config = ConfigFactory.load

  val remote = config.getString("accio.remote")
  val local = config.getString("accio.local")
  val defaultBranch: String

  // Prototype methods
  def lastCommit: String
  def initialize: Unit
  def update(branch: String): Unit
  // def countLines(commit: Commit): Unit
  def getFilePathsInCommit(hash: String): Seq[String]
  def isHead(commit: Commit): Boolean

  // Implemented methods
  def getFilePath(localPath: String) =
    local + File.separator + localPath
  def getFile(localPath: String) = new File(getFilePath(localPath))
}

object RepoModel extends GitModel

