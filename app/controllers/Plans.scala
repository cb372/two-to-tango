package controllers

import com.typesafe.plugin._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import play.api.libs.concurrent.Akka
import com.typesafe.plugin.MailerPlugin
import play.api.Play
import play.api.Play.current
import scala.concurrent.ExecutionContext

object Plans extends Controller {
  
  def list = Action { implicit request =>
    Users.maybeLoggedIn { user =>
      val unmatched = Plan.findUnmatched(limit = None)
      val matched = Plan.findMatched(limit = Some(10))
      Ok(views.html.plans.list(unmatched, matched, user))
    }
  }

  val form = Form(
    tuple(
      "summary" -> text(maxLength = 255),
      "details" -> optional(text(maxLength = 255))
    )
  )

  def showForm = Action { implicit request =>
    Users.loggedIn { _ =>
      Ok(views.html.plans.form(form))
    }
  }

  def create = Action { implicit request =>
    Users.loggedIn { user =>
      form.bindFromRequest.fold ({ formWithErrors =>
        Ok(views.html.plans.form(form))
      }, { case (summary, details) =>
        // Persist to DB
        val plan = Plan.create(summary, details, user)
        Redirect(routes.Plans.list()).flashing("info" -> "Successfully created a plan!")
      })
    }
  }

  def offer(planId: Long) = Action { implicit request =>
    Users.loggedIn { user =>
      Plan.find(planId).fold[PlainResult] {
        NotFound("Sorry, no plan found with that ID")
      } { plan =>
        val updatedPlan = Plan.setOfferer(plan, user)
        sendNotificationMail(updatedPlan)
        Redirect(routes.Plans.list()).flashing("info" -> s"Thanks! ${plan.creator.name} will be pleased!")
      }
    }
  }

  /**
   * Asychronously send an email to the plan owner (CC'ing the offerer),
   * notifying them that the plan has received an offer.
   */
  private def sendNotificationMail(plan: Plan): Unit = {
    if (Play.configuration.getBoolean("emailNotifications.enabled").getOrElse(true)) {
      import scala.concurrent.duration._
      import ExecutionContext.Implicits.global
      Akka.system.scheduler.scheduleOnce(0.seconds) {
        val mail = use[MailerPlugin].email
        mail.addFrom("Two To Tango <no-reply@fake.com>")
        mail.addRecipient(plan.creator.email)
        plan.offerer.map {u => mail.addCc(u.email)}
        mail.setSubject("Good news from Two To Tango!")
        mail.send(notificationMailBody(plan))
      }
    }
  }

  def notificationMailBody(plan: Plan): String = {
    s"""
    |Greetings from Two To Tango.
    |
    |Good news! ${plan.offerer.map(_.name).getOrElse("some dude")} has offered to help with the following project.
    |
    |Summary: ${plan.summary}
    |Details: ${plan.details.getOrElse("(none)")}
    |
    |Happy hacking!
    |""".stripMargin
  }

}

