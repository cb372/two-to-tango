package models

import scalikejdbc._
import scalikejdbc.SQLInterpolation._

/**
 * Author: chris
 * Created: 8/13/13
 */
case class Plan(id: Long, summary: String, details: Option[String],
                creatorId: Long, creator: User,
                offererId: Option[Long], offerer: Option[User])

object Plan extends SQLSyntaxSupport[Plan] {
  override val columnNames = Seq("id", "summary", "details", "creator_id", "offerer_id")

  def apply(p: ResultName[Plan], creator: User, offerer: Option[User])(rs: WrappedResultSet): Plan = Plan(
    id = rs.long(p.c("id")),
    summary = rs.string(p.c("summary")),
    details = rs.stringOpt(p.c("details")),
    creatorId = rs.long(p.c("creator_id")),
    creator,
    offererId = rs.longOpt(p.c("offerer_id")),
    offerer = offerer
  )

  def apply(p: SyntaxProvider[Plan], c: SyntaxProvider[User])(rs: WrappedResultSet): Plan =  {
    val creator = User.apply(c.resultName)(rs)
    apply(p.resultName, creator, None)(rs)
  }

  def apply(p: SyntaxProvider[Plan], c: SyntaxProvider[User], o: SyntaxProvider[User])(rs: WrappedResultSet): Plan =  {
    val creator = User.apply(c.resultName)(rs)
    val offerer = User.opt(o.resultName)(rs)
    apply(p.resultName, creator, offerer)(rs)
  }

  private val p = Plan.syntax("p")
  private val c = User.syntax("u")
  private val o = User.syntax("o")

  def find(id: Long)(implicit session: DBSession = AutoSession): Option[Plan] = withSQL {
    select.from(Plan as p).join(User as c).on(p.creatorId, c.id).leftJoin(User as o).on(p.offererId, o.id).where.eq(p.id, id)
  }.map(Plan(p, c, o)).single().apply()

  def findUnmatched(limit: Option[Int] = None)(implicit session: DBSession = AutoSession): List[Plan] = withSQL {
    withLimit(limit) {
      select.from(Plan as p).join(User as c).on(p.creatorId, c.id).where.isNull(p.offererId).orderBy(p.id).desc
    }
  }.map(Plan(p, c)).list().apply()

  def findMatched(limit: Option[Int] = None)(implicit session: DBSession = AutoSession): List[Plan] = withSQL {
    withLimit(limit) {
      select.from(Plan as p).join(User as c).on(p.creatorId, c.id).join(User as o).on(p.offererId, o.id).orderBy(p.id).desc
    }
  }.map(Plan(p, c, o)).list().apply()

  def create(summary: String, details: Option[String], creator: User)(implicit session: DBSession = AutoSession): Plan = {
    val id = withSQL {
      insert.into(Plan).columns(column.summary, column.details, column.creatorId).values(summary, details, creator.id)
    }.updateAndReturnGeneratedKey().apply()
    Plan(id, summary, details, creator.id, creator, None, None)
  }

  def setOfferer(plan: Plan, offerer: User)(implicit session: DBSession = AutoSession): Plan = {
    withSQL {
      update(Plan as p).set(p.c("offerer_id") -> offerer.id).where.eq(p.c("id"), plan.id)
    }.update().apply()
    plan.copy(offerer = Some(offerer), offererId = Some(offerer.id))
  }

  private def withLimit[A](limit: Option[Int])(query: PagingSQLBuilder[A]): SQLBuilder[A] = {
    limit.fold[SQLBuilder[A]] { query } { lim => query.limit(lim) }
  }
}
