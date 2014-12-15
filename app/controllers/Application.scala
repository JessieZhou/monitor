package controllers

import be.objectify.deadbolt.scala.DeadboltActions
import models._
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.LoggerFactory
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import security.{MyDeadboltHandler}
import util._
import scala.collection.mutable

object Application extends Controller with DeadboltActions{
  val loginForm = Form(
    tuple(
      "email" -> email,
      "password" -> nonEmptyText()
    )
  )

  val registerForm = Form(
    tuple(
      "username" -> nonEmptyText(),
      "email" -> email,
      "password" -> nonEmptyText(),
      "repassword" -> nonEmptyText(),
      "role" -> number(),
      "group" -> longNumber(),
      "team" -> longNumber()
    )
  )

  def index = {
    SubjectPresent(new MyDeadboltHandler(failResult = Ok(views.html.neonlogin(loginForm)))) {
      Action {
        implicit request =>
          val user = User.getUserById(request.session.get("session.id").get.toLong)
          Logger.info(s"${user.email} visit index")
          Ok(views.html.index(user))
      }
    }
  }


  def login = SubjectPresent(new MyDeadboltHandler(failResult = Ok(views.html.neonlogin(loginForm)))){
    Action {
      implicit request =>
        val user = User.getUserById(request.session.get("session.id").get.toLong)
        Logger.info(s"${user.email} autologin")
        Ok(views.html.index(user))
    }
  }

  def logout = Action {
    implicit request =>
      Ok(views.html.neonlogin(loginForm)).withNewSession
  }

  def msg(msg:String) = Action {
    implicit request =>
      val user = User.getUserById(request.session.get("session.id").get.toLong)
      Ok(views.html.index(user))
  }

  def authenticate = Action {
      implicit request =>
        loginForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.neonlogin(formWithErrors)),
          userInfo => {
            val user = User.getUserByEmail(userInfo._1)
            if (user.isDefined) {
              val md5Hex = DigestUtils.md5Hex(userInfo._2+user.get.email)
              if (user.get.password == md5Hex) {
                Logger.info(s"${user.get.email} login OK")
                if(user.get.role == 4){
                  Redirect(routes.Application.index()).withSession("session.id"->user.get.id.toString,"session.password"->user.get.password)
                }else{
                  Redirect(routes.Application.index()).withSession("session.id"->user.get.id.toString,"session.password"->user.get.password)
                }
              } else {
                Logger.info(s"${user.get.email} login fail")
                Ok(views.html.neonlogin(loginForm.fill((userInfo._1, "")).withError("perror", "password error")))
              }
            } else {
              Ok(views.html.neonlogin(loginForm.fill((userInfo._1, "")).withError("merror", "email invalid")))
            }
          }
        )
  }

  def javascriptRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
      )
    ).as("text/javascript")
  }
}
