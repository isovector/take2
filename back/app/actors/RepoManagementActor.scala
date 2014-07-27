package actors

import play.api._
import scala.sys.process._
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import models._

object RepoManagement {
    case object Initialize
    case object Update
}

class RepoManagementActor extends Actor {
    val log = Logging(context.system, this)
    def receive = {
        case RepoManagement.Initialize => sender ! initialize
        case RepoManagement.Update => sender ! update
        case _ => throw new UnsupportedOperationException
    }

    def initialize = { 
        Process(Seq(
            "git", 
            "clone", 
            RepoModel.remote, 
            RepoModel.local
        )).!!
    }

    def update = { 
        Process(Seq(
            "git", 
            "pull", 
            "--all"
        ), new java.io.File(RepoModel.local)).!!
    }
}


