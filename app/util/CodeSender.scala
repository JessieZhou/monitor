package util

import java.io.File
import java.util.Calendar
import java.util.Date;
import java.text.SimpleDateFormat;
import com.typesafe.plugin._
import play.api.{Logger, Play}
import play.api.Play.current
import play.api.libs.concurrent.Akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import models.{User}


class CodeSender {

  protected val mailer: MailerAPI = use[MailerPlugin].email

  protected def delayedExecution(block: => Unit): Unit = Akka.system.scheduler.scheduleOnce(1.seconds)(block)

  def sendCode(code: String, user: User) = {
    delayedExecution {
      Logger.info(s"${user.email} want reset password")
      mailer.setSubject("Password Reset")
      mailer.setRecipient(user.email)
      mailer.setFrom("Rhea Admin <rhea@neotel.com.cn>")
      mailer.sendHtml("")
    }
  }

//  val dayOfWeek = List[String]("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")

  def sendReport(user: User, otherjob: String, helpcontent:String, file: File = null, filename: String = null, filetype: String = null) = {
    delayedExecution {

    }
  }
}

object CodeSender extends CodeSender
