package actors

import play.api._
import scala.sys.process._
import scala.io.Source
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import models._

object RepoManagement {
    case object Initialize
    case object Update
}

// TODO(sandy): is there any reason for this to exist?
class RepoManagementActor extends Actor {
    val log = Logging(context.system, this)
    def receive = {
        case RepoManagement.Initialize => sender ! RepoModel.initialize
        case RepoManagement.Update => RepoModel.update
        case _ => throw new UnsupportedOperationException
    }
}


