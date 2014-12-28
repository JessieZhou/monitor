package models

import anorm.SqlParser._
import anorm._
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current

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


case class UserKeyword(id: Long, uid: Long, kid: Long, auxiliary: Option[String], hid: Option[Long])

object UserKeyword {
  val idCacheKey = "cache.userkeyword.id."
  val keyCacheKey = "cache.userkeyword.key."

  val simple = {
    get[Long]("userkeyword.id") ~
      get[Long]("userkeyword.uid") ~
      get[Long]("userkeyword.kid") ~
      get[Option[String]]("userkeyword.auxiliary") ~
      get[Option[Long]]("userkeyword.hid") map { case id ~ uid ~ kid ~ auxiliary ~ hid=>
      UserKeyword(id, uid, kid, auxiliary, hid)
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

  def updateHid(id: Long, hid: Long) = {
    DB.withConnection { implicit c =>
      SQL("update userkeyword set hid={hid} where id={id}")
        .on('id->id, 'hid->hid)
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

case class KeywordPage(id: Long, ukid: Long, emotion: Int, types: Int, website: String, ctime: Long, url: String, summary: String)

object KeywordPage {
  val simple = {
    get[Long]("keywordpage.id") ~
      get[Long]("keywordpage.ukid") ~
      get[Int]("keywordpage.emotion") ~
      get[Int]("keywordpage.types") ~
      get[String]("keywordpage.website") ~
      get[Long]("keywordpage.ctime") ~
      get[String]("keywordpage.url") ~
      get[String]("keywordpage.summary") map {case id~ukid~emotion~types~website~ctime~url~summary =>
      KeywordPage(id, ukid, emotion, types, website, ctime, url, summary)
    }
  }

  def addKeywordPage(ukid: Long, emotion: Int, types: Int, website: String, ctime: String, url: String, summary: String) = {
    def checkIfNotExist = {
      DB.withConnection{implicit c=>
        SQL("select * from keywordpage where ukid={ukid} and url={url}").on('ukid->ukid, 'url->url).as(simple.singleOpt).isEmpty
      }
    }
    if(checkIfNotExist) {
      DB.withConnection { implicit c =>
        SQL("inset into keywordpage(ukid,emotion,types,website,ctime,url,summary) values({ukid},{emotion},{types},{website},{ctime},{url},{summary})")
          .on('ukid -> ukid, 'emotion -> emotion, 'types -> types, 'website -> website, 'ctime -> ctime, 'url -> url, 'summary -> summary)
          .execute()
      }
    }
  }

  def getByUkid(ukid: Long) = {
    DB.withConnection{implicit c=>
      SQL("select * from keywordpage where ukid={ukid}").on('ukid->ukid).as(simple *)
    }
  }
}