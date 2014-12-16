package models

import anorm.SqlParser._
import anorm._
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current

/**
 * Created by chenlingpeng on 2014/12/16.
 */


case class Keyword(id: Long, uid: Long, keyword: String, auxiliary: Option[String])



object Keyword {
  val idCacheKey = "cache.keyword.id"
  val keyCacheKey = "cache.keyword.key"

  val simple = {
    get[Long]("keyword.id") ~
      get[Long]("keyword.uid") ~
      get[String]("keyword.keyword") ~
      get[Option[String]]("keyword.auxiliary") map { case id ~ uid ~ keyword ~ auxiliary =>
      Keyword(id, uid, keyword, auxiliary)
    }
  }

  def getAllKeywordObj = {
    DB.withConnection{implicit c=>
      SQL("select * from keyword where uid > 0").as(simple *)
    }
  }

  def getKeywordById(id: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from keyword where id={id}").on('id -> id).as(simple.single)
    }
  }

  def getKeywordByIdFromCache(id: Long) = {
    Cache.getOrElse(id.toString){
      getKeywordById(id)
    }
  }

  def getKeywordByName(keyword: String) = {
    DB.withConnection { implicit c =>
      SQL("select * from keyword where keyword={keyword}").on('keyword -> keyword).as(simple.singleOpt)
    }
  }

  def getKeywordByKUid(id: Long, uid: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from keyword where id={id} and uid={uid}").on('id -> id,'uid->uid).as(simple.singleOpt)
    }
  }

  def getKeywordsByUid(uid: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from keyword where uid={uid}").on('uid -> uid).as(simple *)
    }
  }

  def updateKeywordAuxiliary(uid: Long, id: Long, auxiliary: Option[String]) = {
    DB.withConnection { implicit c =>
      SQL("update keyword set auxiliary={auxiliary} where uid={uid} and id={id}")
        .on('uid -> uid, 'id->id, 'auxiliary->auxiliary)
        .executeUpdate()
    }
  }

  def deleteUserKeyword(id: Long, uid: Long) = {
    DB.withConnection { implicit c =>
      SQL("delete from keyword where uid={uid} and id={id}")
        .on('uid -> uid, 'id->id)
        .executeUpdate()
    }
  }

  def addUserKeyword(uid: Long, keyword: String) = {
    val obj = getKeywordByName(keyword)
    if(obj.isDefined){

    }else{
      val maxid = DB.withConnection { implicit c=>
        SQL("select max(id) from keyword").as(scalar[Long].single)
      }+1
      DB.withConnection{implicit c=>
        SQL("insert into keyword(id,uid,keyword) values({id},{uid},{keyword})")
          .on('id->maxid, 'uid->uid, 'keyword->keyword).execute()
      }
    }
  }

}