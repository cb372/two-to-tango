package models

import scalikejdbc._
import com.googlecode.flyway.core.Flyway

trait InMemoryDB {

  val dbUrl = "jdbc:h2:mem:test-in-memory;DB_CLOSE_DELAY=-1"
  val dbUser = "user"
  val dbPassword = "pwd"

  val flyway = new Flyway
  flyway.setDataSource(dbUrl, dbUser, dbPassword)
  flyway.setLocations("db/migration/default")
  flyway.migrate()

  ConnectionPool.singleton(dbUrl, dbUser, dbPassword)

}

