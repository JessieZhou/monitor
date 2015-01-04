package controllers

import models.{PageItem, Stock}
import play.api.libs.json.Json
import play.api.mvc.Controller

/**
 * Created by chenlingpeng on 14-12-21.
 */
object StockApp extends Controller with Secured{
  val idGubaMap = Map(1->"dfcf",2->"hexun",3->"jinrongjie")
  val regCode = "\\d".r

  val siteUrlMap = Map("dfcf"->"")

  def list() = withAuth{userid => implicit request=>
    val stocks = Stock.getByUid(userid.toLong)
    Ok("")
  }

  def addStock() = withAuth{userid => implicit request=>
    val stockName = request.body.asFormUrlEncoded.get("stockname")(0)
    val stockCode = request.body.asFormUrlEncoded.get("stockcode")(0)
    // TODO: should verify code with all number
    val sid = Stock.addStock(userid.toLong, stockCode, stockName)

    Ok("")
  }

  def delStock() = withAuth{userid => implicit request=>
    val sid = request.body.asFormUrlEncoded.get("sid")(0).toLong
    Stock.deleteById(sid)
    Ok("")
  }

  def listBySite(sid: Long, site: String) = withAuth{userid => implicit request=>
    val url = siteUrlMap(site)
    // TODO: get list here
    val items: List[PageItem] = ???

    Ok("")
  }

  def dingtie(url: String, content: String) = withAuth{userid => implicit request=>
    // TODO: add dingtie task to schedule
    Ok(Json.obj("status"->0))
  }

  def fatie(title: String, content: String, site: String) = withAuth{userid => implicit request=>
    // TODO: add fatie task to schedule
    Ok(Json.obj("status"->0))
  }
}
