package controllers

import play.api._
import play.api.mvc._
import models._

object Application extends Controller {
  
  def index = Action {
    val plans = Plan.findAllUnmatched()
    Ok(views.html.index(plans))
  }
  
}