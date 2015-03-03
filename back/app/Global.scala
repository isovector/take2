package models

import play.api._
import com.typesafe.config.ConfigFactory

object Global extends GlobalSettings {
  val config = ConfigFactory.load

  override def onStart(app: Application) {
    // Initialize the repo if it just got stomped by a db evolution
    RepoModel.initialize
  }
}

