package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  
  "Application" should {
    "render the plan list page" in {
      running(FakeApplication()) {
        val home = route(FakeRequest(GET, "/plans")).get
        
        status(home) must equalTo(OK)
        contentType(home) must beSome.which(_ == "text/html")
        contentAsString(home) must contain ("Looking for a buddy!")
      }
    }
  }

}

