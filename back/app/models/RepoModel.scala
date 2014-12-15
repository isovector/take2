package models

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
  val remote = "git@github.com:Paamayim/take2.git"
  //val remote = "git@github.com:Paamayim/well-contributed.git"
  val local = "repo"
  val defaultBranch: String

  // Prototype methods
  def lastCommit: String
  def initialize: Unit
  def update(branch: String): Unit
  def getFilePathsInCommit(hash: String): Seq[String]
  def isHead(commit: Commit): Boolean

  // Implemented methods
  def getFilePath(localPath: String) =
    local + File.separator + localPath
  def getFile(localPath: String) = new File(getFilePath(localPath))

  def buildTagsIndex() = {
    import scala.sys.process._
    import java.io.ByteArrayInputStream

    val ctagsName = ".take2.ctags"

    Seq(
      "ctags",
      "--excmd=numbers",
      "--tag-relative=yes",
      "-f", getFilePath(ctagsName),
      "-R",
      // TODO(sandy): make this configurable
      "--exclude=repo/back/public/javascripts/angular-1.2.1/*",
      "--exclude=repo/back/public/javascripts/bootstrap*",
      "--exclude=repo/back/public/javascripts/jquery*",
      "--exclude=repo/back/public/javascripts/syntax-highlighter/*",
      "--exclude=repo/back/public/javascripts/*.min.js",
      getFilePath("")
    ).!

    Source
      .fromFile(getFile(ctagsName))
      .getLines
      .foreach { line =>
        if (line(0) != '!') {
          Logger.info(line)
          val pieces = line.split("\t")

          Symbol.create(
            pieces(1),
            pieces(0),
            pieces(2).filter(_.isDigit).toInt,
            pieces(3))
        }
    }

    getFile(ctagsName).delete()
  }
}

object RepoModel extends GitModel

