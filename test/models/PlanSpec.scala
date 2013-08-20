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
      val bob = User.create("Bob", "bob@example.com")
      val chris = User.create("Chris", "chris@example.com")

      withSQL { delete.from(Plan) }.update.apply()
      Plan.create("Unmatched plan 1", Some("details 1"), chris)
      Plan.create("Unmatched plan 2", None, bob)
      Plan.setOfferer(Plan.create("Matched plan 1", Some("details 3"), chris), bob)
      Plan.setOfferer(Plan.create("Matched plan 2", Some("details 4"), bob), chris)
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

    "find unmatched plans without limit in descending ID order" in new AutoRollbackWithFixture {
      val unmatchedPlans = Plan.findUnmatched(None)
      unmatchedPlans.size must_==(2)
      unmatchedPlans(0).id must be > (unmatchedPlans(1).id)

      unmatchedPlans(0).summary must_== ("Unmatched plan 2")
      unmatchedPlans(0).details must_== (None)
      unmatchedPlans(0).creator.name must_== ("Bob")
      unmatchedPlans(0).offerer.isDefined must_== (false)
      unmatchedPlans(0).offererId.isDefined must_== (false)

      unmatchedPlans(1).summary must_== ("Unmatched plan 1")
      unmatchedPlans(1).details must_== (Some("details 1"))
      unmatchedPlans(1).creator.name must_== ("Chris")
      unmatchedPlans(1).offerer.isDefined must_== (false)
      unmatchedPlans(1).offererId.isDefined must_== (false)
    }

    "find unmatched plans with limit" in new AutoRollbackWithFixture {
      val unmatchedPlans = Plan.findUnmatched(Some(1))
      unmatchedPlans.size must_==(1)
    }

    "find matched plans without limit in descending ID order" in new AutoRollbackWithFixture {
      val matchedPlans = Plan.findMatched(None)
      matchedPlans.size must_==(2)
      matchedPlans(0).id must be > (matchedPlans(1).id)

      matchedPlans(0).summary must_== ("Matched plan 2")
      matchedPlans(0).details must_== (Some("details 4"))
      matchedPlans(0).creator.name must_== ("Bob")
      matchedPlans(0).offerer.get.name must_== ("Chris")
      matchedPlans(0).offererId.isDefined must_== (true)
    }

    "set the offerer for a plan" in new AutoRollbackWithFixture {
      val chris = User.findByEmail("chris@example.com").get
      val bob = User.findByEmail("bob@example.com").get

      val unmatched = Plan.create("Unmatched plan", Some("details"), chris)
      val matched = Plan.setOfferer(unmatched, bob)
      matched.copy(offerer = None, offererId = None) must_== (unmatched)

      val fromDB = Plan.find(matched.id).get
      fromDB must_== (matched)
    }


  }

}

