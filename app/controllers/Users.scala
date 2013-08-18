package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.User
import play.api.libs.json.Json

/**
 *
 * Author: c-birchall
 * Date:   13/08/14
 */
object Users extends Controller {

  val loginForm = Form(
    single(
      "email" -> text(maxLength = 255)
    )
  )

  val signupForm = Form(
    tuple(
      "name" -> text(maxLength = 255),
      "email" -> text(maxLength = 255)
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
    }, { case mail =>
      val user = User.findByEmail(mail)
      user.fold[PlainResult] {
        // unknown user
        Ok(views.html.users.login(loginForm.bindFromRequest))
      } { u =>
        // Valid user. Save user ID in session and redirect
        Redirect(routes.Plans.list()).withSession(
          session + ("userId" -> u.id.toString)
        )
      }
    })
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.index()).withSession(session - "userId")
  }

  def emailAutoComplete(term: String) = Action { implicit request =>
    val emails = User.findByEmailStartsWith(term).map(_.email)
    Ok(Json.toJson(emails))
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

  def userInfo = Action { implicit request =>
    maybeLoggedIn { user =>
      Ok(views.html.users.userInfo(user))
    }
  }

  /*
   * Helper methods
   */

  def loggedIn[A](block: User => PlainResult)(implicit request: Request[A]): PlainResult = {
    session.get("userId").flatMap { case uId => User.find(uId.toLong) }.fold {
      Redirect(routes.Users.login).flashing("error" -> "Oops, you're not logged in!")
    } { user =>
      block(user)
    }
  }

  def maybeLoggedIn[A](block: Option[User] => PlainResult)(implicit request: Request[A]): PlainResult = {
    val user: Option[User] = session.get("userId").flatMap { case uId => User.find(uId.toLong) }
    block(user)
  }

}
