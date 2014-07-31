package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.Play.current
import com.github.nscala_time.time.Imports._

import models._
import utils._
import utils.DateConversions._

import play.api.db.slick.Config.driver.simple._

object FileMetricsController extends Controller {
    val Table = TableQuery[SnapshotModel]

    def concat(ll:List[List[Any]]): List[Any] = { 
        ll match { 
            case List(Nil) => Nil; 
            case (x::xs) => x ::: concat(xs); 
            case Nil => Nil 
        }
    }

    def listAll(file: String) = Action {
        val users = DB.withSession { implicit session =>
            Table.where(_.file === file).list
        }.groupBy(_.user)

        // Count lines by user
        val totalLinesByUsers = users.toSeq.map {
            case (user, snaps) =>
                user -> snaps.map {
                  snap => snap.lines.toList
                }
        }.map {
            case (user, lists) =>
                user -> concat(lists).groupBy(line => line).map {
                    x => x._1 -> x._2.length 
                }
        }

        import Json._
        Ok(
            Json.toJson(Map(
                "file" -> toJson(file),
                "commit" -> toJson(Todo.unimplemented),
                "userData" -> toJson(totalLinesByUsers.toSeq.map {
                    case (user, snaps) => toJson(Map(
                        "user" -> toJson(user),
                        "timeSpent" -> toJson(users(user).length),
                        "timeSpentByLine" -> toJson(snaps.toSeq.map {
                            case (line, count) => Map(
                                "line" -> toJson(line.asInstanceOf[Int]),
                                "count" -> toJson(count)
                            )
                        })
                    ))
                    }
                )
            ))
        ).as("text/text")
    }

    def getUsersInFile(file: String) = Action {
        val recentStamp = DateTime.now - 5.minutes

        val users = DB.withSession { implicit session =>
        Table.where(_.file.like(file + "%")).where( x =>
                x.timestamp > recentStamp
            ).list
        }.groupBy(_.user).toSeq.map {
            case (k, v) => k
        }

        Ok(
            Json.toJson(users)
        ).as("text/text")
    }

}
