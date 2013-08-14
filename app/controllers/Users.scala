package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.User

/**
 *
 * Author: c-birchall
 * Date:   13/08/14
 */
object Users extends Controller {

  val loginForm = Form(
    single(
      "name" -> text
    )
  )

  val signupForm = Form(
    tuple(
      "name" -> text,
      "email" -> email
    )
  )

  def showLoginForm = Action { implicit request =>
    Ok(views.html.users.login(loginForm))
  }

  def showSignupForm = Action { implicit request =>
    Ok(views.html.users.signup(signupForm))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold({ formWithErrors =>
      // Form not filled in correctly
      Ok(views.html.users.login(formWithErrors))
    }, { case name =>
      val user = User.findByName(name)
      user.fold {
        // unknown user
        Ok(views.html.users.login(loginForm.bindFromRequest)).flashing("error" -> "Unknown user")
      } { u =>
        // Valid user. Save user ID in session and redirect
        Redirect(routes.Plans.list()).withSession(
          session + ("userId" -> u.id.toString)
        )
      }
    })
  }

  def signup = Action { implicit request =>
    signupForm.bindFromRequest.fold({ formWithErrors =>
    // Form not filled in correctly
      Ok(views.html.users.signup(formWithErrors))
    }, { case (name, email) =>
      val user = User.findByName(name)
      user.fold {
        // User does not already exist. Add to DB
        val user = User.create(name, email)

        // Add user ID to session and redirect
        Redirect(routes.Plans.list()).withSession(
          session + ("userId" -> user.id.toString)
        )
      } { u =>
        Ok(views.html.users.signup(signupForm.bindFromRequest)).flashing("error" -> "That user already exists")
      }
    })
  }
}
