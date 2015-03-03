package models

import collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import play.api._

object Global extends GlobalSettings {
  private val config = ConfigFactory.load

  val facebookAppId = config.getString("facebook.appID")
  val facebookSecret = config.getString("facebook.secret")
  val whitelist =
    config.getStringList("accio.whitelist").toList

  override def onStart(app: Application) {
    // Initialize the repo if it just got stomped by a db evolution
    RepoModel.initialize
  }
}

