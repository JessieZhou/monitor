package controllers

import java.io.File

import controllers.Application._
import models.User
import org.apache.commons.codec.digest.DigestUtils
import play.api.{Play, Logger}
import play.api.cache.Cache
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.{TimeFormatUtil, CodeSender, CodeGenerator}
import play.api.Play.current
import scala.concurrent.duration._


/**
 * Created by chenlingpeng on 2014/11/4.
 */
object UserApp extends Controller with Secured{

  def forgetPsw = Action {
    implicit request =>
      val yourEmail = request.body.asFormUrlEncoded.get("yourEmail")(0)
      val user = User.getUserByEmail(yourEmail)
      if(user.nonEmpty){
        val code = CodeGenerator.newCode.toString
        Cache.set(yourEmail, DigestUtils.md5Hex(DigestUtils.md5Hex(code)+yourEmail), Duration(60,MINUTES))
        CodeSender.sendCode(DigestUtils.md5Hex(code), user.get)
        Ok(Json.toJson(1))
      }else{
        Ok(Json.toJson(0))
      }
  }

  def validate(userid: Long, code: String) = Action {
    implicit request =>
      val user = User.getUserById(userid)
      val cachecode = Cache.getAs[String](user.email)
      if(cachecode.nonEmpty){
        if(cachecode.get == DigestUtils.md5Hex(code+user.email)){
          Logger.info(s"${user.email} reset password OK")
          User.forgetPsw(user.email, DigestUtils.md5Hex(DigestUtils.md5Hex("111111")+user.email))
          Ok("密码重置成功！")
        }
        else{
          Ok("密码重置失败！")
        }
      }else{
        Ok("密码重置失败！")
      }

  }

}
