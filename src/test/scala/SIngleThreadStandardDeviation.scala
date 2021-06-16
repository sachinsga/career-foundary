import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source
import net.liftweb.json._
import scala.collection.immutable.ListMap
import scala.math._

object SIngleThreadStandardDeviation {
  case class TestCaseClass(
      time_period_start: Option[String],
      time_period_end: Option[String],
      time_open: Option[String],
      time_close: Option[String],
      price_open: Option[Double],
      price_high: Option[Double],
      price_low: Option[Double],
      price_close: Option[Double],
      volume_traded: Option[Long],
      trades_count: Option[Int]
  )

  def test() = {
    val json = getAPIResponse(
      "http://cf-code-challenge-40ziu6ep60m9.s3-website.eu-central-1.amazonaws.com/ohlcv-btc-usd-history-6min-2020.json"
    )
    implicit val formats = DefaultFormats
    var list: List[TestCaseClass] = parse(json).extract[List[TestCaseClass]]
    list = list.filter(x => {
      x.time_close.getOrElse(null) != null & x.price_close.getOrElse(
        null
      ) != null
    })

    //    list.foreach(x => {
    //      val test = x.time_close.getOrElse(null)
    //      println(test.substring(0,test.indexOf("T")))
    //    })
    val tuples = list.map(x => {

      val time = x.time_close.get
      val price = x.price_close.get
      Tuple2(time.substring(0, time.indexOf("T")), price)
    })
    //    tuples.foreach(println(_))
    val groupedData = tuples.groupBy(_._1)
    val test1: Map[String, List[Double]] =
      groupedData.map(entry => (entry._1, entry._2.map(_._2)))
    val mean: Map[String, (List[Double], Double)] = test1.map(entry => {
      val list = entry._2
      val count = list.size
      val sum = list.reduce((x, y) => x + y)
      val mean = sum / count
      (entry._1, Tuple2(list, mean))
    })

    val stdDav = mean.map(entry => {
      val key = entry._1
      val valueList = entry._2._1
      val valueMean = entry._2._2
      val count = valueList.size

      val newList = valueList.map(item => pow(item - valueMean, 2))
      val sum = newList.reduce(_ + _)
      val stdDav = sqrt(sum / count)
      (key, Tuple2(valueMean, stdDav))
    })
    val final1 = ListMap(stdDav.toSeq.sortBy(_._1): _*)
    final1.foreach(println)
    //    groupedData.foreach( entry=> {
    //      val list = entry._2
    //      val count = list.size
    //      val sum = list.reduce((x,y) => Tuple2("", x._2+y._2))
    //      println(s"date=${entry._1}, sum=${sum._2}, count=${count}, mean=${sum._2/count}")
    //      println("================")
    //    })
  }

  def getAPIResponse(apiUrl: String): String = {
    val httpClient = HttpClientBuilder.create().build()
    println("Start: Calling Api")
    val httpResponse = httpClient.execute(new HttpGet(apiUrl))
    println("End: Calling Api")
    val entity = httpResponse.getEntity
    val responseCode = httpResponse.getStatusLine.getStatusCode

    if (responseCode == 200) {
      val inputStream = entity.getContent
      val content = Source.fromInputStream(inputStream).getLines.mkString
      inputStream.close()
      httpClient.close()

      content
    } else {
      println(
        s"HTTP GET request failed with response code: $responseCode. Exiting Application.."
      )
      null
    }
  }
}
