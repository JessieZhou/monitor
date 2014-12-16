package security

import be.objectify.deadbolt.scala.{DeadboltHandler, DynamicResourceHandler}
import models.{User}
import play.api.mvc.{AnyContent, Request}

/**
 * Created with IntelliJ IDEA.
 * User: chenlingpeng
 * Date: 2014/10/24
 * Time: 10:00
 *
 */
class MyDynamicResourceHandler extends DynamicResourceHandler {
  // meta should be 'project' or 'task'
  override def isAllowed[A](name: String, meta: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = {
    val uid = request.session.get("session.id").get.toLong
    val password = request.session.get("session.password").get
    val user = User.getUserByIdFromCache(uid)
    true
  }

  override def checkPermission[A](permissionValue: String, deadboltHandler: DeadboltHandler, request: Request[A]): Boolean = {
    false
  }
}
