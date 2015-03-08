package models

import com.github.nscala_time.time.Imports._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Logger
import play.api.Play.current

object DashboardModel {
  private val Table = TableQuery[SnapshotModel]

  private def computeExperts[T]
      (grouper: Snapshot => T)
      (dataset: Seq[Snapshot]): Map[T, Float] = {
    val counts =
      dataset
        .groupBy(grouper)
        .map { case(k, v) =>
          k -> v.length
        }
        .toSeq
        .sortBy(-_._2)

    val totals = (0f /: counts)(_ + _._2)
    counts.map { case (k, v) =>
      k -> (v / totals)
    }.toMap
  }

  def getMostActiveUsers(since: DateTime): Seq[(User, Int)] = {
    User.getActiveSince(since)
      .toSeq
      .sortBy(-_._2) // sort by descending viewsigs
      .take(10)
  }

  private def computeExpertise(change: Change, file: RepoFile): Float = {
    val denom = file.adds + file.dels

    if (denom != 0)
      (change.adds + change.dels).toFloat / denom
    else
      1
  }

  def getUserExpertise(user: User): Map[String, Float] = {
    DB.withSession { implicit session =>
      TableQuery[ChangeModel]
        .filter(_.user === user)
        .list
        .filter(RepoFile exists _.file)
        .map { change =>
          val file = RepoFile.getByFile(change.file).get
          change.file -> computeExpertise(change, file)
        }
        .toMap
    }
  }

  def getFileExperts(file: String): Map[User, Float] = {
    val repoFile = RepoFile.getByFile(file).get

    DB.withSession { implicit session =>
      TableQuery[ChangeModel]
        .filter(_.file === file)
        .list
        .map { change =>
          change.user -> computeExpertise(change, repoFile)
        }
        .toMap
    }
  }

  def getAbsoluteExperts(): Map[User, Float] = {
    val expertResults =
      RepoFile
        .getAll
        .map(_._1)
        .map(getFileExperts)

    // FOLD ALL THE MAPS
    (Map[User, Float]() /: expertResults)((a, b) =>
        a ++ b.map{ case(k, v) => k -> (v + a.getOrElse(k, 0f)) } )
  }
}

