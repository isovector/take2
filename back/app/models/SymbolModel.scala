package models

import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

case class Symbol(id: Int, file: String, name: String, var line: Int, kind: String)

object Symbol extends utils.Flyweight {
  type T = Symbol
  type Key = Int

  private val Table = TableQuery[SymbolModel]

  def create(_1: String, _2: String, _3: Int, _4: String) = {
    getById(
      DB.withSession { implicit session =>
        (Table returning Table.map(_.id)) += new Symbol(0, _1, _2, _3, _4)
      }
    )
  }

  def rawGet(id: Key): Option[Symbol] = {
    DB.withSession { implicit session =>
      Table.filter(_.id === id).firstOption
    }
  }

  def synchronizeWithRepo(): Unit = {
    import scala.io._
    import scala.sys.process._
    import java.io.ByteArrayInputStream

    val srcCommit = Memcache.get("lastSymbolCommit")
    lazy val dstCommit = RepoModel.lastCommit

    val ctagsName = ".take2.ctags"

    Seq(
      "ctags",
      "--excmd=numbers",
      "--tag-relative=yes",
      "-f", RepoModel.getFilePath(ctagsName),
      "--sort=no",
      "-R",
      // TODO(sandy): make this configurable
      "--exclude=repo/back/public/javascripts/angular-1.2.1/*",
      "--exclude=repo/back/public/javascripts/bootstrap*",
      "--exclude=repo/back/public/javascripts/jquery*",
      "--exclude=repo/back/public/javascripts/syntax-highlighter/*",
      "--exclude=repo/back/public/javascripts/*.min.js",
      RepoModel.getFilePath("")
    ).!

    val accio = new utils.RpcClient("http://localhost:7432/")
    val ctagsFile = RepoModel.getFile(ctagsName)

    val it =
      Source
        .fromFile(ctagsFile)
        .getLines
        .map { line =>
          line(0) match {
            case '!' => None
            case _ => {
              val pieces = line.split("\t")
              // This is ultra yucky because nowhere else should we be able to
              // create DB-backed objects which aren't in the DB, but we do it
              // anyway because yolo.
              Some(
                new Symbol(
                  -1,
                  pieces(1),
                  pieces(0),
                  pieces(2).filter(_.isDigit).toInt,
                  pieces(3)))
            }
          }
        }
        .flatten
        .buffered

    if (srcCommit == None) {
      // This is the first time we are building symbols, we do not
      // need to migrate
      it.foreach { symbol =>
        Symbol.create(symbol.file, symbol.name, symbol.line, symbol.kind)
      }

      Memcache += "lastSymbolCommit" -> dstCommit
      return
    }

    while (!it.isEmpty) {
      val file = it.head.file
      Logger.info("processing symbols from: " + file)

      withFileSymbols(file) {
        val newSymbols = it.takeWhile(_.file == file).toList
        val oldSymbols = inMemory.sortBy(_.line)

        val oldLinesJson =
          JsObject(
            oldSymbols.map(
              symbol => symbol.line.toString -> JsString(symbol.line.toString)
            ).toSeq)
          .toString

        val newLinesJson =
          accio.translate(
            srcCommit.get,
            dstCommit,
            RepoModel.getFilePath(file),
            RepoModel.local,
            oldLinesJson
          ).asInstanceOf[String]

        val resultLines =
          Json.parse(newLinesJson)
          .asInstanceOf[JsObject]
          .value
          .toMap


        newSymbols.foreach { symbol =>
        }
      }
    }

    ctagsFile.delete()
    Memcache += "lastSymbolCommit" -> dstCommit
  }

  def withFileSymbols(file: String)(func: => Unit): Unit = {
    preload {
      DB.withSession { implicit session =>
        Table.filter(_.file === file).list
      }
    }

    func
    clear()
  }

  implicit def implSeqSymbolId = MappedColumnType.base[Seq[Symbol], String](
    ss => ss.map(_.id).mkString(";"),
    s =>
      s.split(";")
        .filter(_.length > 0)
        .map(_.toInt)
        .map(i => Symbol.getById(i).get)
  )
}

class SymbolModel(tag: Tag) extends Table[Symbol](tag, "Symbol") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def file = column[String]("file")
  def name = column[String]("name")
  def line = column[Int]("line")
  def kind = column[String]("kind")

  val underlying = Symbol.apply _
  def * = (
    id,
    file,
    name,
    line,
    kind
  ) <> (underlying.tupled, Symbol.unapply _)
}

