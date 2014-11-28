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
}

