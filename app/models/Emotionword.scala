package models


import anorm.SqlParser._
import anorm._
import play.api.db.DB
import play.api.Play.current


/**
 * Created by chenlingpeng on 2014/12/16.
 */
case class Emotionword(id: Int, word: String, value: Int)

object Emotionword {
  val simple = {
    get[Int]("emotionword.id") ~
    get[String]("emotionword.word") ~
    get[Int]("emotionword.val") map {case id~word~value =>
        Emotionword(id, word, value)
    }
  }

  lazy val allEmotionWords = getAll

  def getAll = {
    DB.withConnection{implicit c=>
      SQL("select * from emotionword").as(simple *)
    }.map(ew => (ew.word,ew.value)).toMap
  }

  def getEmotion(word: String) = {
    allEmotionWords.getOrElse(word, 0)
  }
}
