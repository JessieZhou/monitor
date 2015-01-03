package util.process

import models.{HistoryPage, KeywordPage}
import play.api.libs.json.Json

/**
 * Created by abc on 14/12/31.
 */
object ProcessChain {
  def process(json: String) = {
    val page = Json.parse(json)
    val title = (page \ "title").toString()
    val summary = (page \ "summary").toString()
    val time = (page \ "time").toString().toLong
//    val content = (page \ "content").toString()
    val url = (page \ "url").toString()
    val website = (page \ "website").toString()
    val types = (page \ "type").toString().toInt
    val ukid = (page \ "ukid").toString().toLong
    val isToday = (page \ "today").toString().toBoolean
    val emotion = EmotionProcessor.process(title, summary)
    if(isToday){
      KeywordPage.addKeywordPage(ukid, title, emotion, types, website, time, url, summary)
    } else {
      HistoryPage.addHistoryPage(ukid, time, title, emotion, url, website, summary)
    }
    ReportProcessor.process(title, emotion, ukid)
  }
}
