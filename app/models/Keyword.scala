package models

import anorm.SqlParser._
import anorm._
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current
import play.api.libs.json.Json
import util.TimeFormatUtil

import scala.collection.mutable.ArrayBuffer

/**
 * Created by chenlingpeng on 2014/12/16.
 */

case class Keyword(id: Long, keyword: String)

object Keyword {
  val simple = {
    get[Long]("keyword.id") ~
      get[String]("keyword.keyword") map {case id~keyword =>
        Keyword(id,keyword)
    }
  }

  val nameCacheKey = "cache.keyword.keyword."
  val idCacheKey = "cache.keyword.id."

  def getByName(keyword: String) = {
    DB.withConnection { implicit c =>
      SQL("select * from keyword where keyword={keyword}").on('keyword -> keyword).as(simple.singleOpt)
    }
  }

  def getByNameFromCache(keyword: String) = {
    Cache.getOrElse(nameCacheKey+keyword){
      getByName(keyword).get
    }
  }

  def getByIdFromCache(id: Long) = {
    def getById = {
      DB.withConnection { implicit c =>
        SQL("select * from keyword where id={id}").on('id -> id).as(simple.single)
      }
    }

    Cache.getOrElse(idCacheKey+id){
      getById
    }
  }

  /**
   *
   * @param keyword
   * @return primary key if success, -1L otherwise
   */
  def addKeyword(keyword: String) = {
    DB.withConnection{implicit c=>
      val kid: Option[Long] = SQL("insert into keyword(keyword) values({keyword})").on('keyword->keyword).executeInsert()
      kid.getOrElse(-1L)
    }
  }
}


case class UserKeyword(id: Long, uid: Long, kid: Long, auxiliary: Option[String]) {
  def jsonObj = {
    Json.obj(
    "id"->id,
    "keyword"->Keyword.getByIdFromCache(kid).keyword
    )
  }
}

object UserKeyword {
  val idCacheKey = "cache.userkeyword.id."
  val keyCacheKey = "cache.userkeyword.key."

  val simple = {
    get[Long]("userkeyword.id") ~
      get[Long]("userkeyword.uid") ~
      get[Long]("userkeyword.kid") ~
      get[Option[String]]("userkeyword.auxiliary") map { case id ~ uid ~ kid ~ auxiliary=>
      UserKeyword(id, uid, kid, auxiliary)
    }
  }

  def getAll = {
    DB.withConnection{implicit c=>
      SQL("select * from userkeyword where uid > 0").as(simple *)
    }
  }

  def getByIdFromCache(id: Long) = {
    def getById(id: Long) = {
      DB.withConnection { implicit c =>
        SQL("select * from userkeyword where id={id}").on('id -> id).as(simple.single)
      }
    }
    
    Cache.getOrElse(idCacheKey+id){
      getById(id)
    }
  }

  def getByUid(uid: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from userkeyword where uid={uid}").on('uid -> uid).as(simple *)
    }
  }

  def updateAuxiliary(id: Long, auxiliary: Option[String]) = {
    DB.withConnection { implicit c =>
      SQL("update userkeyword set auxiliary={auxiliary} where id={id}")
        .on('id->id, 'auxiliary->auxiliary)
        .execute()
    }
  }

  def deleteById(id: Long) = {
    DB.withConnection { implicit c =>
      SQL("update userkeyword set uid={uid} where id={id}")
        .on('uid -> -1L, 'id->id)
        .execute()
    }
  }

  def addUserKeyword(uid: Long, keyword: String) = {
    def addUserKeyword(kid: Long) = {
      DB.withConnection{implicit c=>
        val ukid: Option[Long] = SQL("insert into userkeyword(uid,kid) values({uid},{kid})").on('uid->uid,'kid->kid).executeInsert()
        ukid.getOrElse(-1L)
      }
    }

    val kw = Keyword.getByName(keyword)
    if(kw.isDefined){
      addUserKeyword(kw.get.id)
    } else {
      val kid = Keyword.addKeyword(keyword)
      addUserKeyword(kid)
    }
  }

}

case class KeywordPage(id: Long, ukid: Long, title: String, emotion: Int, types: Int, website: String, ctime: Long, url: String, summary: String) {
  def jsonObj = Json.obj(
    "id"->id,
    "title"->title,
    "emotion"->emotion,
    "types"->types,
    "website"->website,
    "ctime"->ctime,
    "url"->url,
    "summary"->summary
  )
}

object KeywordPage {
  val simple = {
    get[Long]("keywordpage.id") ~
      get[Long]("keywordpage.ukid") ~
      get[String]("keywordpage.title")~
      get[Int]("keywordpage.emotion") ~
      get[Int]("keywordpage.types") ~
      get[String]("keywordpage.website") ~
      get[Long]("keywordpage.ctime") ~
      get[String]("keywordpage.url") ~
      get[String]("keywordpage.summary") map {case id~ukid~title~emotion~types~website~ctime~url~summary =>
      KeywordPage(id, ukid, title, emotion, types, website, ctime, url, summary)
    }
  }

  def addKeywordPage(ukid: Long, title: String, emotion: Int, types: Int, website: String, ctime: Long, url: String, summary: String) = {
    def checkIfNotExist = {
      DB.withConnection{implicit c=>
        SQL("select * from keywordpage where ukid={ukid} and url={url}").on('ukid->ukid, 'url->url).as(simple.singleOpt).isEmpty
      }
    }
    if(checkIfNotExist) {
      DB.withConnection { implicit c =>
        SQL("inset into keywordpage(ukid,title,emotion,types,website,ctime,url,summary) values({ukid},{title},{emotion},{types},{website},{ctime},{url},{summary})")
          .on('ukid -> ukid, 'title->title, 'emotion -> emotion, 'types -> types, 'website -> website, 'ctime -> ctime, 'url -> url, 'summary -> summary)
          .execute()
      }
    }
  }

  def getByUkid(ukid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid={ukid}").on('ukid->ukid).as(simple *)
    }
  }


  // TODO:
  def getTodayByUkid(ukid: Long) = {
    val (begin, end) = TimeFormatUtil.getToday
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid={ukid} and ctime>{begin} and ctime<{end}")
        .on('ukid->ukid,'begin->begin,'end->end).as(simple *)
    }
  }

  def getByUkid(ukid: Long, pagenum: Int, pagesize: Int = 15) = {
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid={ukid} limit {pagesize} offset {offset}")
        .on('ukid->ukid, 'pagesize->pagesize, 'offset->(pagenum-1)*pagesize)
      .as(simple *)
    }
  }

  // 重要舆情
  def getRecentPages(ukid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid = {ukid} and emotion<>0 order by ctime desc limit 10")
        .on('ukid->ukid).as(simple *)
    }
  }

  def getNegPages(ukid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid={ukid} and emotion<0 order by ctime desc limit 10")
        .on('ukid->ukid).as(simple *)
    }
  }

  // TODO:
  def getPagesWithFilter(ukid: Long, emotion: Int, today: Boolean, page: Int = 0) = {
    DB.withConnection{implicit c=>
      val filter = new ArrayBuffer[String]()
      filter += "ukid={ukid}"
      filter += {
        emotion match {
          case pos if pos > 0 => "emotion>0"
          case neg if neg < 0 => "emotion<0"
          case plain if plain == 0 => "emotion=0"
        }
      }
      if(today){

      }
      SQL("select * from keywordpage where ")
    }
  }

  // TODO
  def emotionStatistic(ukid: Long) = {
    val pages = getByUkid(ukid)
    var pos = 0
    var neg = 0
    var plain = 0
    pages.foreach{(page: KeywordPage)=>
      if(page.emotion>0) pos+=1
      else if(page.emotion<0) neg+=1
      else plain+=1
    }
    (pos,plain,neg)
  }

  def emotionStatisticToday(ukid: Long) = {
    val pages = getTodayByUkid(ukid)
    var pos = 0
    var neg = 0
    var plain = 0
    pages.foreach{(page: KeywordPage)=>
      if(page.emotion>0) pos+=1
      else if(page.emotion<0) neg+=1
      else plain+=1
    }
    (pos,plain,neg)
  }

  // TODO:
  def emotionTrend(ukid: Long) = {
    val pages = getByUkid(ukid)
  }

}