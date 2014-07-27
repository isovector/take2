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

import play.api.db.slick.Config.driver.simple._

object FileMetricsController extends Controller {
    private var Table = TableQuery[SnapshotModel]

    def concat(ll:List[List[Any]]): List[Any] = { 
        ll match { 
            case List(Nil) => Nil; 
            case (x::xs) => x ::: concat(xs); 
            case Nil => Nil 
        }
    }

    def listAll(file: String) = Action {
        val snap = new Snapshot(
            None, 
            DateTime.now, 
            "test",
            2,
            "recent",
            List(2, 6)
        )

        snap.insert()

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

        Logger.info(totalLinesByUsers.mkString)

        import Json._
        Ok(
            Json.toJson(Map(
                "file" -> toJson(file),
                "commit" -> toJson("unimplemented"),
                "userData" -> toJson(totalLinesByUsers.toSeq.map {
                    case (uid, snaps) => toJson(Map(
                        "name" -> toJson("unimplemented"),
                        "id" -> toJson(uid),
                        "timeSpent" -> toJson(users(uid).length),
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
}
