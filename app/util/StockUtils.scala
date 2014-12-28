package util

import java.net.URLEncoder
import java.util

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpPost, HttpGet}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import play.api.Logger

/**
 * Created by chenlingpeng on 2014/12/28.
 */
object StockUtils {

  object DFCF {
    val usernames = List("wuxiu9218@sina.com", "abcddcba2331", "15568411120", "1330493726@qq.com", "18610061662")
    val psws = List("920108", "password1!", "cai317903131", "cai317903131", "pop789789")


    val reg01 = ".*,(.*),(.*)\\.html".r
    var next = 0

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

    def dfcfDingtie(url: String, content: String) = {
      val httpClient = login
      val reg01(code, topic_id) = reg01.findFirstMatchIn(url).get
      val httpPost = new HttpPost(url)
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
      httpPost.setEntity(new UrlEncodedFormEntity(urlParameters))
      val response = httpClient.execute(httpPost)
      Logger.info(EntityUtils.toString(response.getEntity))
      EntityUtils.consume(response.getEntity)
      httpClient.close()
    }
  }
}
