package controllers

import models._
import org.slf4j.LoggerFactory
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import util._
import scala.collection.mutable

object Application extends Controller with Secured {

  // dashboard
  def index = withAuth { username => implicit request =>
      val user = User.getUserByEmailFromCache(username)
      val keywords = UserKeyword.getByUid(user.id)
      Ok(views.html.index(User.getUserByEmail(username).get))
    }



  def msg(msg: String) = Action { implicit request =>
    val user = User.getUserById(request.session.get("session.id").get.toLong)
    Ok(views.html.index(user))
  }



  def javascriptRoutes = Action { implicit request =>
    Ok(Routes.javascriptRouter("jsRoutes")()).as("text/javascript")
  }
}

trait Secured {

  def username(request: RequestHeader) = request.session.get(Security.username)

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.UserApp.login)

  def withAuth(f: => String => Request[AnyContent] => Result) = {
    Security.Authenticated(username, onUnauthorized) { user =>
      Action(request => f(user)(request))
    }
  }
}