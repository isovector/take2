package utils

import play.api.data._
import play.api.libs.json._

import com.github.nscala_time.time.Imports._
import models._

object JSON {
    import Json._

    def apply[T](obj: T)(implicit m: reflect.Manifest[T]): JsValue = obj match {
        case seq: Seq[Any] => toJson(seq.map(JSON(_)))
        case map: Map[String, Any] => toJson(map.map{ case (k, v) => k -> JSON(v)})

        case bool: Boolean => toJson(bool)
        case int: Int => toJson(int)
        case long: Long => toJson(long)
        case str: String => toJson(str)

        case date: DateTime => toJson(date)
        case user: User => toJson(user)

        case js: JsValue => js
        case bad => throw new Exception(m.toString)
    }

    // given a map of names to functions, create a JSON object
    // by calling the functions on src
    def asObj[T](src: T)(maps: Tuple2[String, T => Any]*): JsValue = {
        toJson(maps.map {
            case(k, v) => k -> JSON(v(src))
        }.toMap)
    }


    // add mapJs and asjs functions to everything
    implicit def implSeqToRich[T](underlying: Seq[T]): RichJsonSeq[T] =
        new RichJsonSeq(underlying)

    implicit def implArrayToRich[T](underlying: Array[T]): RichJsonSeq[T] =
        new RichJsonSeq(underlying.toSeq)

    implicit def implAnyToRich[T](underlying: T): RichJsonAny[T] =
        new RichJsonAny(underlying)
}

case class RichJsonSeq[T](underlying: Seq[T]) {
    def mapJs(maps: Tuple2[String, T => Any]*): JsValue = {
        Json.toJson(underlying.map { item =>
            JSON.asObj(item)(maps: _*)
        })
    }
}

case class RichJsonAny[T](underlying: T) {
    def asJs(maps: Tuple2[String, T => Any]*): JsValue = {
        Json.toJson(JSON.asObj(underlying)(maps: _*))
    }

    def toJs(implicit m: reflect.Manifest[T]): JsValue = {
        JSON(underlying)
    }
}

