package controllers

import models.{Keyword, UserKeyword}
import play.api.libs.json.{JsNumber, JsString, JsObject, Json}
import play.api.mvc.Controller

/**
 * Created by chenlingpeng on 14-12-20.
 */
object UserKeywordApp extends Controller with Secured{

  def deleteUserkeyword() = withAuth { userid => implicit request =>
    val ukid = request.body.asFormUrlEncoded.get("ukid")(0).toLong
    val result = UserKeyword.deleteById(ukid)
    if(result){
      Ok(Json.obj("statuc"->0))
    }else{
      Ok(Json.obj("statuc"->1))
    }
  }

  def addUserkeyword() = withAuth{userid => implicit request=>
    val keyword = request.body.asFormUrlEncoded.get("keyword")(0)
    val ukid = UserKeyword.addUserKeyword(userid.toLong, keyword)
    if(ukid>0) {
      Ok(Json.obj("statuc"->0,"ukid"->ukid))
    } else {
      Ok(Json.obj("statuc"->1))
    }
  }
  
  def list() = withAuth{userid => implicit request=>
    val uid = userid.toLong
    val uklist = UserKeyword.getByUid(uid)
    Ok("")
  }

}
