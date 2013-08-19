package models

import scalikejdbc._, config._

trait InMemoryDB {

  if (! ConnectionPool.isInitialized()) {

    ConnectionPool.singleton("jdbc:h2:mem:test-in-memory", "user", "pwd")

    val ddls = new java.io.File("conf/db/migration/default").listFiles.toList.sortWith { (a,b) => a.getName < b.getName }
    DB autoCommit { implicit s =>
      ddls foreach { ddl =>
        SQL(scala.io.Source.fromFile(ddl).getLines.mkString("\n")).execute.apply()
      }
    }
  }

}

