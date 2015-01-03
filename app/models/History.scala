package models


import anorm.SqlParser._
import anorm._
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current

/**
 * Created by chenlingpeng on 2014/12/16.
 */
case class HistoryPage(id: Long, hid:Long, ctime: Long, title: String, emotion: Int, url: String, source: String, summary: String)

object HistoryPage {
  val simple = {
    get[Long]("historypage.id") ~
      get[Long]("historypage.ukid") ~
      get[Long]("historypage.ctime") ~
      get[String]("historypage.title") ~
      get[Int]("historypage.emotion") ~
      get[String]("historypage.url") ~
      get[String]("historypage.source") ~
      get[String]("historypage.summary") map { case id ~ ukid ~ ctime ~ title ~ emotion ~ url ~ source  ~ summary =>
      HistoryPage(id, ukid, ctime, title, emotion, url, source, summary)
    }
  }

  def addHistoryPage(ukid: Long, ctime: Long, title: String, emotion: Int, url: String, source: String, summary: String) = {
    DB.withConnection { implicit c =>
      SQL("insert into historypage(ukid,ctime,title,emotion,url,source,summary) values({ukid},{ctime},{title},{emotion},{url},{source},{summary})")
        .on('ukid->ukid, 'ctime -> ctime, 'title -> title, 'emotion -> emotion, 'url -> url, 'source -> source,  'summary -> summary)
        .execute()
    }
  }

  /**
   * return true if url exist
   * @param url the url checked
   * @return
   */
  def checkIfExist(ukid: Long, url: String) = {
    val page = DB.withConnection{implicit c=>
      SQL("select * from historypage where ukid={ukid} and url={url}").on('ukid->ukid, 'url->url).as(simple.singleOpt)
    }
    page.isDefined
  }

  def getByUkid(ukid: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from historypage where ukid={ukid}").on('ukid -> ukid).as(simple *)
    }
  }

  // for pagin
  def getByUkid(ukid: Long, pagenum: Int, pagesize: Int = 15) = {
    DB.withConnection { implicit c =>
      SQL("select * from historypage where ukid={ukid} limit {pagesize} offset {offset}").on('ukid -> ukid, 'pagesize -> pagesize, 'offset -> (pagenum - 1) * pagesize).as(simple *)
    }
  }

}


case class HistoryKeyword(id: Long, ukid: Long, begin: Long, end: Long, flag: Int, aux: Option[String])

object HistoryKeyword {
  val idCacheKey = "cache.historykeyword.id."
  val simple = {
    get[Long]("historykeyword.id") ~
      get[Long]("historykeyword.ukid") ~
      get[Long]("historykeyword.begin") ~
      get[Long]("historykeyword.end") ~
      get[Int]("historykeyword.flag") ~
      get[Option[String]]("historykeyword.aux") map { case id ~ kid ~ begin ~ end ~ flag ~ aux =>
      HistoryKeyword(id, kid, begin, end, flag, aux)
    }
  }

  /**
   *
   * @param ukid
   * @param begin
   * @param end
   * @param aux
   * @return hid that user add, using to update userkeyword hid
   */
  def addHistoryKeyword(ukid: Long, begin: Long, end: Long, aux: Option[String]) = {
    DB.withConnection { implicit c =>
      val id: Option[Long] = SQL("insert into historykeyword(kid,begin,end,aux) values({ukid},{begin},{end},{aux})")
        .on('ukid -> ukid, 'begin -> begin, 'end -> end, 'aux -> aux)
        .executeInsert()
      id.get
    }
  }

  def getByIdFromCache(id: Long) = {
    def getById = {
      DB.withConnection{implicit c=>
        SQL("select * from historykeyword where id={id}").on('id->id).as(simple.single)
      }
    }

    Cache.getOrElse(idCacheKey+id) {
      getById
    }
  }


}