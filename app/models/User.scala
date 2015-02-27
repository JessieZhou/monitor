package models


import anorm.SqlParser._
import anorm._
import play.api.cache.Cache
import play.api.db.DB
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/22
 * Time: 20:08
 *
 */
// role 1(team),2(group),3(member)
case class User(id: Long,
                email: String,
                password: String,
                reportphone: Option[String],
                reportemail: Option[String],
                needemail: Int,
                needphone: Int
                 )
object User {

  private val idCacheKey = "cache.user.id."
  private val emailCacheKey = "cache.user.email."

  val simple = {
    get[Long]("user.id") ~
      get[String]("user.email") ~
      get[String]("user.password") ~
      get[Option[String]]("user.reportphone") ~
      get[Option[String]]("user.reportemail") ~
      get[Int]("user.needemail") ~
      get[Int]("user.needphone") map { case id ~ email ~ password ~ reportphone ~ reportemail ~ needemail ~ needphone  =>
      User(id, email, password, reportphone, reportemail, needemail, needphone)
    }
  }

  def getAll = {
    DB.withConnection { implicit c =>
      SQL("select * from user").as(simple *)
    }
  }

  def getUserById(id: Long) = {
    DB.withConnection { implicit c =>
      SQL("select * from user where id={id}").on('id -> id).as(simple.single)
    }
  }

  def getUserByIdFromCache(id: Long) = {
    Cache.getOrElse[User](idCacheKey + id) {
      getUserById(id)
    }
  }

  def getUserByEmail(email: String) = {
    DB.withConnection { implicit c =>
      SQL("select * from user where email={email}").on('email -> email).as(simple.singleOpt)
    }
  }

  def getUserByEmailFromCache(email: String) = {
    Cache.getOrElse[Option[User]](emailCacheKey + email) {
      getUserByEmail(email)
    }
  }

  def addUser(email: String, password: String) = {
    DB.withConnection{implicit c=>
      val uid: Option[Long] = SQL("insert into user(email,password) values({email},{password})").on('email->email, 'password->password).executeInsert()
      uid.getOrElse(-1L)
    }
  }

  def updateEmailSet(id: Long, reportemail: Option[String], needemail: Int) = {
    val c = DB.withConnection{implicit c =>
      SQL("update user set reportemail={reportemail} and needemail={needemail} where id={id}")
      .on('reportemail->reportemail, 'needemail->needemail, 'id->id)
      .executeUpdate()
    }
    if(c==1){
      // todo: get from cache
      val user = getUserById(id)
      Cache.set(idCacheKey+id, user)
      Cache.set(emailCacheKey+user.email, Some(user))
    }
  }

  def updatePhoneSet(id: Long, reportphone: Option[String], needphone: Int) = {
    val c = DB.withConnection{implicit c =>
      SQL("update user set reportphone={reportphone} and needphone={needphone} where id={id}")
        .on('reportphone->reportphone, 'needphone->needphone, 'id->id)
        .executeUpdate()
    }
    if(c==1){
      // todo: get from cache
      val user = getUserById(id)
      Cache.set(idCacheKey+id, user)
      Cache.set(emailCacheKey+user.email, Some(user))
    }
  }

  def authenticate(email: String, password: String): Boolean = {
    val user = getUserByEmail(email)
    if (user.isDefined) {
      user.get.password == password
    } else {
      false
    }
  }

  def resetPsw(id: Long, old:String, email: String, password: String) = {
    DB.withConnection { implicit c =>
      val res = SQL("update user set password = {password} where email = {email} and password = {old} and id={id}")
        .on('password -> password, 'email -> email, 'old -> old,'id->id).executeUpdate()
      if(res>0){
        val user = User.getUserById(id)
        Cache.set(emailCacheKey+email,Some(user))
        Cache.set(idCacheKey+user.id,user)
      }
      res>0
    }
  }

  def forgetPsw(email: String, password: String) = {
    DB.withConnection { implicit c =>
      SQL("update user set password = {password} where email = {email}")
        .on('password -> password, 'email -> email).executeUpdate()
    }
  }

  def emailExist(email: String) = {
    DB.withConnection{implicit c=>
      SQL("select * from user where email={email}").on('email->email).as(simple.singleOpt).isDefined
    }
  }
}
