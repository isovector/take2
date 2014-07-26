package utils

import com.github.nscala_time.time.Imports._
import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp

object DateConversions {
    val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    implicit def dateTimeSlick  =
      MappedColumnType.base[DateTime, String](
        dt => dateFormatter.print(dt.getMillis),
        ts => dateFormatter.parseDateTime(ts)
    )

    implicit def durationSlick  =
      MappedColumnType.base[Duration, Long](
        d => d.millis,
        l => new Duration(l)
    )
}

