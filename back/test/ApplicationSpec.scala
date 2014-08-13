import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import play.api.test.{FakeApplication, FakeRequest, WithApplication}
import play.api.test.Helpers._

import models.{Snapshot, User}


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  val routes = Seq(
    ("/", GET, true),
    ("/api/snapshot", GET, false),
    ("/api/snapshot", POST, false),
    ("/api/snapshot/2", DELETE, false),
    ("/directory", GET, true),
    ("/directory/", GET, true),
    ("/directory/test", GET, false),
    ("/manage-repo/initialize", GET, false),
    ("/api/currently/open", GET, false),
    ("/api/currently/viewing/test", GET, false),
    ("/api/metrics/all/test", GET, false),
    ("/api/metrics/popular/123", GET, false),
    ("/api/search/directory/test", GET, false)
  )

  "Application" should {
    routes foreach { case(path, method, render) =>
      if (render) {
        "render " + path in new WithApplication{
          val r = route(FakeRequest(method, path)).get

          status(r) must equalTo(OK)
          contentType(r) must beSome.which(_ == "text/html")
        }
      } else {
        "have api " + path in new WithApplication{
          route(FakeRequest(method, path)) must beSome
        }
      }
    }

    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/boum")) must beNone
    }
  }

  // Need to use real db connection somehow
  //"Snapshot" should {
  //  "return records" in new WithApplication(FakeApplication(
  //      additionalConfiguration=inMemoryDatabase())) {
  //    val user = User(
  //        name="JOHN",
  //        email="john@john.com",
  //        lastActivity=DateTime.now())
  //    user.save()
  //    val snapshot = Snapshot(
  //      timestamp=DateTime.now(),
  //      file="test.py",
  //      user=user,
  //      commit="asdfasfd",
  //      lines=Seq(1)
  //    )
  //    "Hellow world" must endWith ("world")
  //  }
  //}
}

// vim: ts=2 sw=2
