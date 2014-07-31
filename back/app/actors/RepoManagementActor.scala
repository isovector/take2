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

class RepoManagementActor extends Actor {
    val log = Logging(context.system, this)
    def receive = {
        case RepoManagement.Initialize => sender ! initialize
        case RepoManagement.Update => update
        case _ => throw new UnsupportedOperationException
    }

    def initialize = { 
        val cmd = Process(Seq(
            "git", 
            "clone", 
            "-v",
            RepoModel.remote, 
            RepoModel.local
        ))

        var lines = Stream[String]();
        var count = 0;
        val pio = new ProcessIO(
            _ => (),
            stdout => 
                Source.fromInputStream(stdout).getLines.foreach (
                    _ => count = count + 1
                ),
            stderr => 
                Source.fromInputStream(stderr).getLines.foreach (
                    _ => count = count + 1
                )
        )

        // okay so this doesn't work great, but you know, whatever
        val proc = cmd.run(pio)
        Thread.sleep(5000)

        if (count < 2) {
            proc.destroy
            "error: no keys"
        } else {
            "success"
        }
    }

    def update = { 
        GitModel.update
    }
}


