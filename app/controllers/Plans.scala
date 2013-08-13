package controllers

import play.api._
import play.api.mvc._
import models._

object Plans extends Controller {
  
  def list = Action {
    val plans = Plan.findAllUnmatched()
    if (plans.isEmpty)
      Ok(views.html.noplans())
    else
      Ok(views.html.index(plans))
  }

  def form = Action {
    Ok("TODO form")
  }

  def create = Action {
    val form = Form(mapping(
      "summary" -> text,
      "details" -> optional(text)
    ))
    Ok("TODO")
  }
  
}

