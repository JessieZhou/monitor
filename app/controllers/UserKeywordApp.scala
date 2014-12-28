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
      Ok(Json.obj("status"->0))
    }else{
      Ok(Json.obj("status"->1))
    }
  }

  def addUserkeyword() = withAuth{userid => implicit request=>
    val keyword = request.body.asFormUrlEncoded.get("keyword")(0)
    val ukid = UserKeyword.addUserKeyword(userid.toLong, keyword)
    if(ukid>0) {
      Ok(Json.obj("status"->0,"ukid"->ukid))
    } else {
      Ok(Json.obj("status"->1))
    }
  }

  val auxSeprator = "#####"
  
  def deleteKeywordAux() = withAuth { userid => implicit request =>
    val ukid = request.body.asFormUrlEncoded.get("ukid")(0).toLong
    val aux = request.body.asFormUrlEncoded.get("aux")(0)
    val keyword = UserKeyword.getByIdFromCache(ukid)
    val auxNew = keyword.auxiliary.getOrElse("").split(auxSeprator).filter(!_.equals(aux)).mkString(auxSeprator) match {
      case "" => None
      case a => Some(a)
    }

    if(UserKeyword.updateAuxiliary(ukid, auxNew)){
      Ok(Json.obj("status"->0))
    }else{
      Ok(Json.obj("status"->1))
    }
  }

  def addKeywordAux() = withAuth{userid => implicit request=>
    val ukid = request.body.asFormUrlEncoded.get("ukid")(0).toLong
    val aux = request.body.asFormUrlEncoded.get("aux")(0)
    val keyword = UserKeyword.getByIdFromCache(ukid)
    val auxNew = keyword.auxiliary.getOrElse("")+aux+auxSeprator

    if(UserKeyword.updateAuxiliary(ukid, Some(auxNew))){
      Ok(Json.obj("status"->0))
    }else{
      Ok(Json.obj("status"->1))
    }
  }
  
  def list() = withAuth{userid => implicit request=>
    val uid = userid.toLong
    val uklist = UserKeyword.getByUid(uid)
    Ok("")
  }

}
