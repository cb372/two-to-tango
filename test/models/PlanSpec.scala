package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable.Specification

import org.joda.time._
import scalikejdbc._, SQLInterpolation._

class PlanSpec extends Specification with InMemoryDB {

  val pl = Plan.syntax("p")

  sequential

  trait AutoRollbackWithFixture extends AutoRollback {
    override def fixture(implicit session: DBSession) {
      withSQL { delete.from(User) }.update.apply()
      User.create("Alice", "alice@example.com")
      User.create("Bob", "bob@example.com")
      val chris = User.create("Chris", "chris@example.com")

      withSQL { delete.from(Plan) }.update.apply()
      Plan.create("More Testing!", Some("Writing tests for models."), chris)
    }
  }

  private def existingId(implicit s: DBSession): Long = withSQL { 
   select.from(Plan as pl).limit(1) 
  }.map(_.long(pl.resultName.id)).single.apply.get

  "Plan" should {

    "find by primary keys" in new AutoRollbackWithFixture {
      val maybePlan = Plan.find(existingId)
      maybePlan.isDefined must_==(true)
    }

  }

}

