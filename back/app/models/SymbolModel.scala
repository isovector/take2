package models

import play.api.data._
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB
import play.api.libs.json._
import play.api.Logger
import play.api.Play.current

case class Symbol(
    id: Int,
    file: String,
    name: String,
    line: Int,
    kind: String) {
  // loosely equal
  def ~==(other: Symbol): Boolean = {
    file == other.file &&
      name == other.name &&
      kind == other.kind
  }

  def needsUpdate(other: Symbol): Boolean = {
    !((this ~== other) && line == other.line)
  }

  object unsafe {
    def insert() = {
      Symbol.create(file, name, line, kind)
    }
  }
}

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
      it.foreach(_.unsafe.insert())
      Memcache += "lastSymbolCommit" -> dstCommit
      return
    }

    Logger.info(srcCommit.get)
    Logger.info(dstCommit)


    while (!it.isEmpty) {
      val file = it.head.file
      Logger.info("processing symbols from: " + file)

      withFileSymbols(file) {
        val newSymbols = it.takeWhile(_.file == file).toList
        val oldSymbols = inMemory.sortBy(_.line)

        val oldLinesJson =
          JsObject(
            oldSymbols.map(
              symbol => symbol.line.toString -> JsNumber(symbol.line)
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
          .map { case (k, v) => k.toInt -> v.as[Int] }
          .toMap

        newSymbols.foreach { newSymbol =>
          findOldSymbol(
            newSymbol, newSymbols, oldSymbols, resultLines
          ) match {
            case Some(oldSymbol) => {
              if (newSymbol.needsUpdate(oldSymbol)) {
                DB.withSession { implicit session =>
                  Table
                    .filter(_.id === oldSymbol.id)
                    .update(
                      newSymbol.copy(id = oldSymbol.id))
                }
              }
            }

            case None            => newSymbol.unsafe.insert()
          }
        }
      }
    }

    ctagsFile.delete()
    Memcache += "lastSymbolCommit" -> dstCommit
  }

  def findOldSymbol(
      newSymbol: Symbol,
      newSymbols: Seq[Symbol],
      oldSymbols: Seq[Symbol],
      newToOld: Map[Int, Int]): Option[Symbol] = {
    newToOld.get(newSymbol.line) flatMap { oldLine =>
      val oldSymbol = oldSymbols.find(_.line == oldLine).get
      if (oldSymbol ~== newSymbol) {
        // The symbol has only moved
        Some(oldSymbol)
      } else if (oldSymbol.kind == newSymbol.kind &&
          !newSymbols.exists(_.name == oldSymbol.name)) {
        // There is no new symbol with the name that used to be here
        // so it is a rename
        Some(oldSymbol)
      } else {
        // Relatively major refactoring; nothing to do.
        None
      }
    }
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

