package models

import scalikejdbc.specs2.mutable.AutoRollback
import org.specs2.mutable.Specification

import org.joda.time._
import scalikejdbc._, SQLInterpolation._

class UserSpec extends Specification with InMemoryDB {

  val u = User.syntax("u")

  sequential

  trait AutoRollbackWithFixture extends AutoRollback {
    override def fixture(implicit session: DBSession) {
      withSQL { delete.from(User) }.update.apply()
      User.create("Alice", "alice@example.com")
      User.create("Bob", "bob@example.com")
      User.create("Chris", "chris@example.com")
    }
  }

  private def existingId(implicit s: DBSession): Long = withSQL { 
   select.from(User as u).limit(1) 
  }.map(_.long(u.resultName.id)).single.apply.get

  "User" should {

    "find by primary keys" in new AutoRollbackWithFixture {
      val maybeUser = User.find(existingId)
      maybeUser.isDefined must_==(true)
    }

    "find by name" in new AutoRollbackWithFixture {
      val maybeUser = User.findByName("Alice")
      maybeUser.get.email must_==("alice@example.com")
    }

    "find by email" in new AutoRollbackWithFixture {
      val maybeUser = User.findByEmail("chris@example.com")
      maybeUser.get.name must_==("Chris")
    }

    "find by email which starts with the prefix" in new AutoRollbackWithFixture {
      val foundUsers = User.findByEmailStartsWith("bob@e")
      foundUsers.size must_==(1)
      foundUsers.head.name must_==("Bob")
    }

  }

}

