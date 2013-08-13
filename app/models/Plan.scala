package models

import scalikejdbc._
import scalikejdbc.SQLInterpolation._

/**
 * Author: chris
 * Created: 8/13/13
 */
case class Plan(id: Long, summary: String, details: Option[String], creatorId: Long, creator: User)

object Plan extends SQLSyntaxSupport[Plan] {
  override val columnNames = Seq("id", "summary", "details", "creator_id")

  def apply(p: ResultName[Plan], creator: User)(rs: WrappedResultSet): Plan = Plan(
    id = rs.long("id"),
    summary = rs.string("summary"),
    details = rs.stringOpt("details"),
    creatorId = rs.long("creator_id"),
    creator
  )

  def apply(p: SyntaxProvider[Plan], u: SyntaxProvider[User])(rs: WrappedResultSet): Plan =  {
    val creator = User.apply(u.resultName)(rs)
    apply(p.resultName, creator)(rs)
  }

  private val p = Plan.syntax("p")
  private val u = User.syntax("u")

  def find(id: Long)(implicit session: DBSession = AutoSession): Option[Plan] = withSQL {
    select.from(Plan as p).join(User as u).on(p.creatorId, u.id).where.eq(p.id, id)
  }.map(Plan(p, u)).single().apply()

  def findByCreator(creator: User)(implicit session: DBSession = AutoSession): List[Plan] = withSQL {
    select.from(Plan as p).where.eq(p.creatorId, creator.id)
  }.map(Plan(p.resultName, creator)).list().apply()

  def findAllUnmatched()(implicit session: DBSession = AutoSession): List[Plan] = withSQL {
    select.from(Plan as p).join(User as u).on(p.creatorId, u.id).orderBy(p.id).desc
  }.map(Plan(p, u)).list().apply()

  def create(summary: String, details: Option[String], creator: User)(implicit session: DBSession = AutoSession): Plan = {
    val id = withSQL {
      insert.into(Plan).columns(column.summary, column.details, column.creatorId).values(summary, details, creator.id)
    }.updateAndReturnGeneratedKey().apply()
    Plan(id, summary, details, creator.id, creator)
  }
}
