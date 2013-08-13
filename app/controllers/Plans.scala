package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

object Plans extends Controller {
  
  def list = Action {
    val plans = Plan.findAllUnmatched()
    if (plans.isEmpty)
      Ok(views.html.plans.noplans())
    else
      Ok(views.html.plans.list(plans))
  }

  val form = Form(
    tuple(
      "summary" -> text,
      "details" -> optional(text)
    )
  )

  def showForm = Action {
    Ok(views.html.plans.form(form))
  }

  def create = Action {
    Ok("TODO")
  }
  
}

