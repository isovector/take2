import models._
import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    // Initialize the repo if it just got stomped by a db evolution
    RepoModel.initialize
  }
}

