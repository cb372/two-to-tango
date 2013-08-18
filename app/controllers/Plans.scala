package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

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
        Plan.setOfferer(plan, user)
        Redirect(routes.Plans.list()).flashing("info" -> s"Thanks! ${plan.creator.name} will be pleased!")
      }
    }
  }

}

