package models

import scalikejdbc._
import scalikejdbc.SQLInterpolation._

/**
 * Author: chris
 * Created: 8/12/13
 */
case class User(id: Long, name: String, email: String)

object User extends SQLSyntaxSupport[User] {

  override val columnNames = Seq("id", "name", "email")

  def apply(u: ResultName[User])(rs: WrappedResultSet): User = User(
    id = rs.long(u.id),
    name = rs.string(u.name),
    email = rs.string(u.email)
  )

  def opt(u: ResultName[User])(rs: WrappedResultSet): Option[User] =
    rs.longOpt(u.id) map { _ => apply(u)(rs) }

  private val u = User.syntax("u")

  def find(id: Long)(implicit session: DBSession = AutoSession): Option[User] = withSQL {
    select.from(User as u).where.eq(u.id, id)
  }.map(User(u.resultName)).single.apply()

  def findByName(name: String)(implicit session: DBSession = AutoSession): Option[User] = withSQL {
    select.from(User as u).where.eq(u.name, name)
  }.map(User(u.resultName)).single.apply()

  def findByEmail(email: String)(implicit session: DBSession = AutoSession): Option[User] = withSQL {
    select.from(User as u).where.eq(u.email, email)
  }.map(User(u.resultName)).single.apply()

  def findByEmailStartsWith(email: String)(implicit session: DBSession = AutoSession): List[User] = withSQL {
    select.from(User as u).where.like(u.email, s"${email}%")
  }.map(User(u.resultName)).list.apply()

  def create(name: String, email: String)(implicit session: DBSession = AutoSession): User = {
    val id = withSQL {
      insert.into(User).namedValues(column.name -> name, column.email -> email)
    }.updateAndReturnGeneratedKey.apply()
    User(id, name, email)
  }

}
