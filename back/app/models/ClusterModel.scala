package models

import com.github.nscala_time.time.Imports._
import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

case class Cluster(
    id: Int,
    user: User,
    created: DateTime,
    var snapshots: Seq[Snapshot],
    var rawFiles: String) {
  lazy private val Table = TableQuery[ClusterModel]

  // Necessary to allow LIKE queries on the files
  def files = rawFiles.split(";").filter(_.length != 0).toSet

  def addFile(file: String) = {
    rawFiles = (files + file).mkString(";")
  }

  def save() = {
    DB.withSession { implicit sesion =>
      Table.filter(_.id === id).update(this)
    }
  }
}

object Cluster extends utils.Flyweight {
  type T = Cluster
  type Key = Int

  val clusterTime = 5 minutes

  lazy private val Table = TableQuery[ClusterModel]

  def rawGet(id: Key): Option[Cluster] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }

  def create(_1: User, _2: DateTime, _3: Seq[Snapshot], _4: String) = {
    getById(
      DB.withSession { implicit session =>
        (Table returning Table.map(_.id)) += new Cluster(0, _1, _2, _3, _4)
      }
    ).get
  }

  def getByUserAndTime(user: User, when: DateTime): Cluster = {
    val after = when - clusterTime

    val candidates =
      DB.withSession { implicit sesion =>
        Table
          .filter(_.user === user)
          .list
      }

    candidates
      .filter(_.created >= after)
      .headOption
      .map(cluster => getById(cluster.id).get)
      .getOrElse {
        // Clean up flyweights
        reclaim(after)
        Snapshot.reclaim(after)

        create(user, when, Seq(), "")
      }
  }

  object unmanaged {
    def getClustersWithFile(file: String): Seq[Cluster] = {
      val likeQuery = s"%$file%"

      DB.withSession { implicit session =>
        Table
          .filter(_.files like likeQuery)
          .list
      }
    }
  }

  def getClusteredSymbols(symbol: Symbol): Map[Symbol, Float] = {
    val file = symbol.file
    val fileClusters = unmanaged.getClustersWithFile(file)

    val clusters =
      fileClusters.filter(_.snapshots.exists(_.symbols.contains(symbol)))

    val symbols =
      clusters
        .flatMap(
          _.snapshots
            .filter(_.file != file)
            .flatMap(_.symbols)
        )

    val total = symbols.length
    symbols
      .groupBy(_.id)
      .map { case (k, v) =>
        Symbol.getById(k).get -> v.length.toFloat / total
      }
  }
}

class ClusterModel(tag: Tag) extends Table[Cluster](tag, "Cluster") {
  import Snapshot._
  import utils.DateConversions._

  def id        = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def user      = column[User]("user")
  def created   = column[DateTime]("created")
  def snapshots = column[Seq[Snapshot]]("snapshots", O.DBType("TEXT"))
  def files     = column[String]("files", O.DBType("TEXT"))

  val underlying = Cluster.apply _
  def * = (
    id,
    user,
    created,
    snapshots,
    files
  ) <> (underlying.tupled, Cluster.unapply _)
}

