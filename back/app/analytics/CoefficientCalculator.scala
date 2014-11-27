package analytics

import com.github.nscala_time.time.Imports._
import models._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.Logger
import play.api.Play.current
import scala.io.Source

object CoefficientCalculator {
    // Compute how much a user knows about a file
    // (the math here is pretty sketchy)
    def generateUserCoefficients = {
        // How many snapshots * lines you need to be considered an expert
        val expertLevelMultiplier = 10

        val users = User.getAll()
        users map { user =>
            val snapshots = Snapshot.getByUser(user)
            Snapshot.lineviews(_.file)(snapshots).map { case (file, lines) =>
                val numLines = Source.fromFile(
                    RepoModel.getFile(file)).getLines().length
                val lineViews = lines.values.sum

                math.min(lineViews / (numLines * expertLevelMultiplier), 1.0)
            }
        }
    }

    // Compute how likely dest is to be open at the same time as src
    def generateFileCoefficients: Map[(String, String), Float] = {
        val slidingWindow = 10
        val clusterTime = 5 minutes

        val totalInstances = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)
        val clusteredWith = scala.collection.mutable.Map[(String, String), Int]().withDefaultValue(0)
        User.getAll().map { user =>
            val snapshots = Snapshot.getByUser(user)
            snapshots.sliding(slidingWindow).toList.map { window =>
                val before = window.head.timestamp + clusterTime
                val cluster = window.filter(_.timestamp <= before).map(_.file).toSet

                cluster.map { file => totalInstances(file) += 1 }
                cluster.toSeq.combinations(2).toList.map { choose =>
                    clusteredWith((choose(0), choose(1))) += 1
                    clusteredWith((choose(1), choose(0))) += 1
                }
            }
        }

        Memcache += "coeffTimestamp" -> DateTime.now.getMillis.toString
        clusteredWith.toMap.map { case ((src, dest), count) =>
            (src, dest) -> (count.toFloat / totalInstances(src))
        }
    }
}

