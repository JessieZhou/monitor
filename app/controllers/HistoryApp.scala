package controllers

import models.{HistoryPage, HistoryKeyword, Keyword, UserKeyword}
import play.api.libs.json.{JsNumber, JsString, JsObject, Json}
import play.api.mvc.Controller

/**
 * Created by chenlingpeng on 2014/12/28.
 */
object HistoryApp extends Controller with Secured {
  def history(ukid: Long) = withAuth { username => implicit request =>
    Ok("")
  }

  def timeset() = withAuth { username => implicit request =>
    val ukid = request.body.asFormUrlEncoded.get("ukid")(0).toLong
    val start = request.body.asFormUrlEncoded.get("start")(0).toLong
    val end = request.body.asFormUrlEncoded.get("end")(0).toLong

    val hid = HistoryKeyword.addHistoryKeyword(ukid, start, end, UserKeyword.getByIdFromCache(ukid).auxiliary)
    if(hid>0 && UserKeyword.updateHid(ukid, hid)){
      Ok(Json.obj("status"->0))
    } else {
      Ok(Json.obj("status"->1))
    }
  }

  def historyview(ukid: Long) = withAuth { username => implicit request =>
    val hid = UserKeyword.getByIdFromCache(ukid).hid.get
    val historyNews = HistoryPage.getByHid(hid)
    Ok("")
  }

  def historypageview(ukid: Long, page: Int) = withAuth { username => implicit request =>
    val hid = UserKeyword.getByIdFromCache(ukid).hid.get
    val historyNews = HistoryPage.getByHid(hid, page)
    Ok("")
  }
}
