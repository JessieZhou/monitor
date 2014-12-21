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
    Cache.getOrElse[User](emailCacheKey + email) {
      getUserByEmail(email).get
    }
  }

  def addUser(email: String, password: String) = {
    DB.withConnection{implicit c=>
      val uid: Option[Long] = SQL("insert into user(email,password) values({email},{password})").on('email->email, 'password->password).executeInsert()
      uid.getOrElse(-1L)
    }
  }

  def updateEmailSet(id: Int, email: String, needemail: Int) = {
    DB.withConnection{implicit c =>
      SQL("update user set email={email} and needemail={needemail} where id={id}")
      .on('email->email, 'needemail->needemail, 'id->id)
      .executeUpdate()
    }
  }

  def updatePhoneSet(id: Int, phone: String, needphone: Int) = {
    DB.withConnection{implicit c =>
      SQL("update user set phone={phone} and needphone={needphone} where id={id}")
        .on('phone->phone, 'needphone->needphone, 'id->id)
        .executeUpdate()
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

  def resetPsw(old:String, email: String, password: String) = {
    DB.withConnection { implicit c =>
      val res = SQL("update user set password = {password} where email = {email} and password = {old}")
        .on('password -> password, 'email -> email, 'old -> old).executeUpdate()
      if(res>0){
        val user = User.getUserByEmail(email)
        Cache.set(emailCacheKey+email,user)
        Cache.set(idCacheKey+user.get.id,user)
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
}
