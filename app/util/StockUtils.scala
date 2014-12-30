package util

import java.net.URLEncoder
import java.util
import java.util.concurrent.{TimeUnit, Executors}

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.Logger

import scala.collection.mutable

/**
 * Created by chenlingpeng on 2014/12/28.
 */
object StockUtils {

  object DFCF {
    val usernames = List("wuxiu9218@sina.com", "abcddcba2331", "15568411120", "1330493726@qq.com", "18610061662")
    val psws = List("920108", "password1!", "cai317903131", "cai317903131", "pop789789")

    val reg01 = ".*,(.*),(.*)\\.html".r
    var next = 0

    val scheduledThreadPool = Executors.newSingleThreadScheduledExecutor()

    def init() = {
      Logger.info("init dfcf schedule")
      scheduledThreadPool.scheduleAtFixedRate(new FatieWorker(),1L, 3L, TimeUnit.MINUTES)
    }

    case class FatieTask(number: String, title: String, content: String)

    val queue = new mutable.Queue[FatieTask]()

    private class FatieWorker extends Runnable {
      override def run() = {
        Logger.info("定时任务开始")
        if(queue.nonEmpty){
          val item = queue.dequeue()
          Logger.info("发帖："+item.title+" in "+item.number)
          fatie(item)
          Logger.info("发帖结束")
        }
      }
    }

    private def login = {
      val username = usernames(next)
      val password = psws(next)
      next = (next + 1) % usernames.size
      val loginURL = "http://passport.eastmoney.com/guba/AjaxAction.ashx?cb=jQuery18309460038826800883_1411024516265&op=login&dlm=" + URLEncoder.encode(username, "utf-8") + "&mm=" + URLEncoder.encode(password, "utf-8") + "&vcode=&_=1411024534921"
      val httpGet = new HttpGet(loginURL)
      httpGet.addHeader("Referer", "http://guba.eastmoney.com/")
      httpGet.addHeader("Host", "passport.eastmoney.com")
      val httpClient = HttpClientUtil.getHttpClient
      val response = httpClient.execute(httpGet)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient
    }

    def dingtie(url: String, content: String) = {
      val httpClient = login
      val reg01(code, topic_id) = reg01.findFirstMatchIn(url).get
      val httpPost = new HttpPost("http://guba.eastmoney.com/action.aspx")
      httpPost.addHeader("Host", "guba.eastmoney.com")
      httpPost.addHeader("Origin", "http://guba.eastmoney.com")
      httpPost.addHeader("Referer", url)
      httpPost.addHeader("X-Requested-With", "XMLHttpRequest")

      val urlParameters:util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("action", "review3"))
      urlParameters.add(new BasicNameValuePair("topic_id", topic_id))
      urlParameters.add(new BasicNameValuePair("huifu_id", ""))
      urlParameters.add(new BasicNameValuePair("text", content))
      urlParameters.add(new BasicNameValuePair("code", code))
      urlParameters.add(new BasicNameValuePair("yzm", ""))
      urlParameters.add(new BasicNameValuePair("yzm_id", ""))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters,"gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def fatie(number: String, title: String, content: String) = {
      this.synchronized {
        queue.enqueue(FatieTask(number, title, content))
      }
    }

    private def fatie(task: FatieTask) = {
      val httpClient = login
      val httpPost = new HttpPost("http://guba.eastmoney.com/action.aspx")
      val host = "guba.eastmoney.com"
      val origin = "http://guba.eastmoney.com"
      val ref = "http://guba.eastmoney.com/list," + task.number + ".html"
      httpPost.addHeader("Host", host)
      httpPost.addHeader("Origin", origin)
      httpPost.addHeader("Referer", ref)
      httpPost.addHeader("X-Requested-With", "XMLHttpRequest")

      val urlParameters:util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("action", "add3"))
      urlParameters.add(new BasicNameValuePair("yuan_id", "0"))
      urlParameters.add(new BasicNameValuePair("title", task.title))
      urlParameters.add(new BasicNameValuePair("text", task.content))
      urlParameters.add(new BasicNameValuePair("code", task.number))
      urlParameters.add(new BasicNameValuePair("pdf", ""))
      urlParameters.add(new BasicNameValuePair("pic", ""))
      urlParameters.add(new BasicNameValuePair("postvalid", "1"))
      urlParameters.add(new BasicNameValuePair("yzm_id", ""))
      urlParameters.add(new BasicNameValuePair("yzm", ""))
      urlParameters.add(new BasicNameValuePair("quanxian", "0"))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters,"gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def list(number: String) = {

    }
  }


  object JRJ {
    val reg01 = ".*,(.*),(.*)\\.html".r

    def fatie(number: String, title: String, content: String) {
      val httpClient = HttpClientUtil.getHttpClient
      val httpPost = new HttpPost("http://istock.jrj.com.cn/topicaddsingle.jspa")
      val urlParameters: util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("anonym", ""))
      urlParameters.add(new BasicNameValuePair("forumid", number))
      urlParameters.add(new BasicNameValuePair("upfilename", ""))
      urlParameters.add(new BasicNameValuePair("upfilelink", ""))
      urlParameters.add(new BasicNameValuePair("Detail", content))
      urlParameters.add(new BasicNameValuePair("upfilepath", ""))
      urlParameters.add(new BasicNameValuePair("showMessage", "0"))
      urlParameters.add(new BasicNameValuePair("Title", title))
      urlParameters.add(new BasicNameValuePair("detail", ""))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, "gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def dingtie(url: String, content: String) = {
      val reg01(code, topic_id) = reg01.findFirstMatchIn(url).get
      val httpClient = HttpClientUtil.getHttpClient
      val httpPost = new HttpPost("http://istock.jrj.com.cn/postadd.jspa")
      val urlParameters: util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("anonym","0"))
      urlParameters.add(new BasicNameValuePair("forumid",code))
      urlParameters.add(new BasicNameValuePair("TopicID",topic_id))
      urlParameters.add(new BasicNameValuePair("hiddenYinYong",""))
      urlParameters.add(new BasicNameValuePair("Detail",content))
      urlParameters.add(new BasicNameValuePair("upfilepath",""))
      urlParameters.add(new BasicNameValuePair("upfilelink",""))
      urlParameters.add(new BasicNameValuePair("upfilename",""))
      urlParameters.add(new BasicNameValuePair("Title","嘿嘿"))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, "gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def list(number: String) = {

    }
  }

  object HEXUN {
    val reg01 = ".*,(.*),(.*)\\.html".r

    def fatie(number: String, title: String, content: String) {
      val httpClient = HttpClientUtil.getHttpClient
      val httpPost = new HttpPost("http://istock.jrj.com.cn/topicaddsingle.jspa")
      val urlParameters: util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("anonym", ""))
      urlParameters.add(new BasicNameValuePair("forumid", number))
      urlParameters.add(new BasicNameValuePair("upfilename", ""))
      urlParameters.add(new BasicNameValuePair("upfilelink", ""))
      urlParameters.add(new BasicNameValuePair("Detail", content))
      urlParameters.add(new BasicNameValuePair("upfilepath", ""))
      urlParameters.add(new BasicNameValuePair("showMessage", "0"))
      urlParameters.add(new BasicNameValuePair("Title", title))
      urlParameters.add(new BasicNameValuePair("detail", ""))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, "gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def dingtie(url: String, content: String) = {
      val reg01(code, topic_id) = reg01.findFirstMatchIn(url).get
      val httpClient = HttpClientUtil.getHttpClient
      val httpPost = new HttpPost("http://istock.jrj.com.cn/postadd.jspa")
      val urlParameters: util.ArrayList[NameValuePair] = new util.ArrayList[NameValuePair]
      urlParameters.add(new BasicNameValuePair("anonym","0"))
      urlParameters.add(new BasicNameValuePair("forumid",code))
      urlParameters.add(new BasicNameValuePair("TopicID",topic_id))
      urlParameters.add(new BasicNameValuePair("hiddenYinYong",""))
      urlParameters.add(new BasicNameValuePair("Detail",content))
      urlParameters.add(new BasicNameValuePair("upfilepath",""))
      urlParameters.add(new BasicNameValuePair("upfilelink",""))
      urlParameters.add(new BasicNameValuePair("upfilename",""))
      urlParameters.add(new BasicNameValuePair("Title","嘿嘿"))
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, "gbk"))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }

    def list(number: String) = {

    }
  }
}
