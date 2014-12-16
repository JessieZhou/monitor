package models


import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current

/**
 * Created by chenlingpeng on 2014/12/16.
 */
case class HistoryPage(id: Long, ctime: Long, title: String, emotion: Int, url: String, source: String, kid: Int, summary: String)

object HistoryPage {
  val simple = {
    get[Long]("historyword.id") ~
    get[Long]("historyword.ctime") ~
    get[String]("historyword.title") ~
    get[Int]("historyword.emotion") ~
    get[String]("historyword.url") ~
    get[String]("historyword.source") ~
    get[Int]("historyword.kid")~
    get[String]("historyword.summary") map {case id~ctime~title~emotion~url~source~kid~summary =>
      HistoryPage(id, ctime, title, emotion, url, source, kid, summary)
    }
  }

  def getHistoryPageByKid(kid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from historyword where kid={kid}")
      .on('kid->kid).as(simple *)
    }
  }

  // for pagin
  def getHistoryPageByKid(kid: Long, pagenum: Int, pagesize: Int = 15) = {
    DB.withConnection{implicit c=>
      SQL("select * from historyword where kid={kid} limit {pagesize} offset {offset}")
        .on('kid->kid, 'pagesize -> pagesize, 'offset->(pagenum-1)*pagesize).as(simple *)
    }
  }
}
