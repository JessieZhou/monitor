package util.process

import models.{User, UserKeyword}

/**
 * Created by abc on 14/12/31.
 */
object ReportProcessor {
  def process(title: String, emotion: Int, ukid: Long) = {
    if(emotion != 0) {
      val uid = UserKeyword.getByIdFromCache(ukid).uid
      val user = User.getUserByIdFromCache(uid)
      if (user.needemail > 0 && user.reportemail.isDefined) {

      }
      if (user.needphone > 0 && user.reportphone.isDefined) {

      }
    }
  }
}
