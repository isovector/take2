package utils

import com.github.nscala_time.time.Imports._
import play.api.db.slick.Config.driver.simple._
import java.sql.Timestamp

object DateConversions {
    implicit def dateTimeSlick  =
      MappedColumnType.base[DateTime, Long](
        dt => dt.getMillis,
        ts => new DateTime(ts)
    )

    implicit def durationSlick  =
      MappedColumnType.base[Duration, Long](
        d => d.millis,
        l => new Duration(l)
    )
}

