package models

import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

/**
 * Created by chenlingpeng on 2014/12/20.
 */
case class Stock(id: Long, uid: Long, stockid: String, stockname: String)

object Stock {
  val simple = {
    get[Long]("stock.id") ~
      get[Long]("stock.uid") ~
      get[String]("stock.stockid") ~
      get[String]("stock.stockname") map {case id~uid~stockid~stockname =>
      Stock(id, uid, stockid, stockname)
    }
  }

  def getByUid(uid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from stock where uid={uid}").on('uid->uid).as(simple *)
    }
  }

  def addStock(uid: Long, stockid: String, stockname: String) = {
    DB.withConnection{implicit c=>
      val sid: Option[Long] = SQL("insert into stock(uid, stockid, stockname) values({uid},{stockid},{stockname})")
        .on('uid->uid,'stockid->stockid,'stockname->stockname).executeInsert()
      sid.getOrElse(-1L)
    }
  }

  def deleteById(id: Long) = {
    DB.withConnection{implicit c=>
      SQL("delete from stock where id={id}").on('id->id).execute()
    }
  }
}

case class ExternalWebsite(id: Long, uid: Long, url: String)

object ExternalWebsite {
  val simple = {
    get[Long]("externalwebsite.id") ~
      get[Long]("externalwebsite.uid") ~
      get[String]("externalwebsite.url") map {case id~uid~url =>
      ExternalWebsite(id, uid, url)
    }
  }

  def addExternalWebsite(uid: Long, url: String) = {
    DB.withConnection{implicit c=>
      val wid: Option[Long] = SQL("insert into externalwebsite(uid,url) values({uid},{url})").on('uid->uid,'url->url).executeInsert()
      wid.getOrElse(-1L)
    }
  }

  def getByUid(uid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from externalwebsite where uid={uid}").on('uid->uid).as(simple *)
    }
  }

  def deleteById(id: Long) = {
    DB.withConnection{implicit c=>
      SQL("delete from externalwebsite where id={id}").on('id->id).execute()
    }
  }
}