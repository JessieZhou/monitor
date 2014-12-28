import play.api.{Application, Logger, GlobalSettings}
import util.StockUtils

/**
 * Created by chenlingpeng on 2014/12/28.
 */
object Global extends GlobalSettings{
  override def onStart(app: Application) {
    Logger.info("Application has started")
    StockUtils.DFCF.dfcfDingtie("http://guba.eastmoney.com/news,600569,138593349.html", "对的呢")
  }
}
