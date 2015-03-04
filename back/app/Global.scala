package models

import collection.JavaConversions._
import com.typesafe.config.ConfigFactory
import play.api._

object Global extends GlobalSettings {
  private val config = ConfigFactory.load

  val (facebookAppID, facebookSecret) =
    if (Play.isDev(Play.current))
      (config.getString("facebook.dev.appID"),
      config.getString("facebook.dev.secret"))
    else
      (config.getString("facebook.prod.appID"),
      config.getString("facebook.prod.secret"))

  val whitelist = config.getStringList("accio.whitelist").toList

  override def onStart(app: Application) {
    // Initialize the repo if it just got stomped by a db evolution
    RepoModel.initialize
  }
}

