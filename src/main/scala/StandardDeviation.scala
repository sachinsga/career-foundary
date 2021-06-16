import com.typesafe.config.ConfigFactory

import scala.io.Source
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._

import java.sql.{DriverManager, ResultSet, SQLException}
import java.util.Properties

object StandardDeviation {

  case class Bitcoin(
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

  def main(args: Array[String]): Unit = {
    val content = Source.fromFile(args(0)).mkString
    val config = ConfigFactory.parseString(content)

    val username = config.getString(Constants.DB_USERNAME)
    val password = config.getString(Constants.DB_PASSWORD)
    val host = config.getString(Constants.DB_HOST)
    val port = config.getString(Constants.DB_PORT)
    val database = config.getString(Constants.DB_DATABASE)
    val table = config.getString(Constants.DB_TABLE)

    println(username, host, port, database, table)

    val spark = SparkSession
      .builder()
      .appName("StandardDeviation")
      .getOrCreate()

    val json = Source
      .fromURL(
        "http://cf-code-challenge-40ziu6ep60m9.s3-website.eu-central-1.amazonaws.com/ohlcv-btc-usd-history-6min-2020.json"
      )
      .mkString
    import spark.implicits._
    var df = spark.read.json(Seq(json).toDS())
    df = df
      .withColumn("day", to_date(col("time_close")))
      .filter(col("day").isNotNull)
    df = df
      .groupBy("day")
      .agg(
        stddev_pop("price_close").alias("stddev")
      )
      .orderBy("day")

    println("=================================")
    println("writing into mysql")
    println("=================================")

    val prop = new Properties()
    prop.setProperty("user", username)
    prop.setProperty("password", password)
    prop.setProperty("driver", "com.mysql.cj.jdbc.Driver")
    val url =
      s"jdbc:mysql://${host}:${port}/${database}"
    createIfNotExist(url, prop, table)
    df.write
      .mode(SaveMode.Overwrite)
      .option("numPartitions", 10)
      .jdbc(
        url,
        table,
        prop
      )
  }

  def createIfNotExist(
      url: String,
      prop: Properties,
      tableName: String
  ) = {
    Class.forName("com.mysql.cj.jdbc.Driver")
    val con = DriverManager.getConnection(url, prop)
    val statement = con.createStatement
    var resultSet: ResultSet = null
    var tableExist: Boolean = false
    try {
      resultSet = statement.executeQuery(
        "SELECT count(1) "
          + "FROM btc_stddev_daily "
      )
      resultSet.next()
      val count = resultSet.getInt(1)
      println(s"count=${count}")
      if (count != null & count != 0)
        tableExist = true
    } catch {
      case e: SQLException =>
        println("Exception occured " + e.printStackTrace())
    }
    println(s"tableExist=${tableExist}")
    if (!tableExist) {
      val createQuery = "CREATE TABLE `btc_stddev_daily` (" +
        "`day` date ," +
        "`stddev` double" +
        ")"
      statement.executeUpdate(createQuery)
    }
    con.close()
  }
}
