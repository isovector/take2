package models

import com.github.nscala_time.time.Imports._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.Play.current

import utils._
import utils.DateConversions._

case class User(
        var id: Option[Int] = None,
        name: String,
        email: String,
        var lastActivity: DateTime) {
    private val Table = TableQuery[UserModel]
    def insert() = {
        // Ensure this Event hasn't already been put into the database
        id match {
            case Some(_) => throw new CloneNotSupportedException
            case None => // do nothing
        }

        DB.withSession { implicit session =>
            id = Some((Table returning Table.map(_.id)) += this)
        }
    }

    def save() = {
        id match {
            case Some(_) => // do nothing
            case None => throw new NullPointerException
        }

        DB.withSession { implicit session =>
            Table.filter(_.id === id.get).update(this)
        }
    }
}

object User {
    private val Table = TableQuery[UserModel]

    def getAll(): Seq[User] = {
        DB.withSession { implicit session =>
            Table.list
        }
    }

    def getById(id: Int): Option[User] = {
        DB.withSession { implicit session =>
            Table.filter(_.id === id).firstOption
        }
    }

    def getByEmail(email: String): Option[User] = {
        DB.withSession { implicit session =>
            Table.filter(_.email === email).firstOption
        }
    }

    implicit val implicitUserWrites = new Writes[User] {
        def writes(user: User): JsValue = {
            Json.obj(
                "id" -> user.id.get,
                "name" -> user.name,
                "email" -> user.email,
                "picture" -> Todo.unimplemented,
                "lastActivity" -> user.lastActivity
            )
        }
    }

    implicit def implicitUserColumnMapper = MappedColumnType.base[User, Int](
        u => u.id.get,
        i => User.getById(i).get
    )
}

class UserModel(tag: Tag) extends Table[User](tag, "User") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def email = column[String]("email")
    def lastActivity = column[DateTime]("lastActivity")

    val user = User.apply _
    def * = (id.?, name, email, lastActivity) <> (user.tupled, User.unapply _)
}

