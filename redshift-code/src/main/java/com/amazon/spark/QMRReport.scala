package com.amazon.spark

import com.amazon.clusters.RedshiftExecutor
import org.apache.spark.sql.SparkSession
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.functions._
import org.json.{JSONArray, JSONObject}
import org.json4s.jsonwritable

import java.util
import scala.collection.JavaConverters.asJavaIterableConverter
import scala.collection.mutable

object QMRReport {


  def extractTableName(query: String, regex: String): Array[String] = {
    val re = regex.r

    return re.findAllMatchIn(query.toLowerCase()).map(x => x.toString().trim).toArray
  }


  def flatArray(input: scala.collection.mutable.WrappedArray[scala.collection.mutable.WrappedArray[String]]): mutable.WrappedArray[String] = {
    val flatArray = input.flatMap(v => v).distinct
    return flatArray
  }

  def mains(args: Array[String]): Unit = {

    val q = "[INSERT /*** jobrun_id::6305908227 job_id::14329794 ***/ INTO public.T_CHANGED_OPLS_PACKAGE_ITEMS select distinct gmp.fulfillment_shipment_id, gmp.package_id, min(gmp.attempted_delivery_date) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification IN ('DeliveryAttempted', 'DeliveryReceived') and gmp.attempted_delivery_date = gmp.event_date then gmp.attempted_delivery_date else null end rows between unbounded preceding and unbounded following) as first_attempted_delivery_date, min(gmp.actual_delivery_date) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification = 'DeliveryReceived' then gmp.actual_delivery_date else null end rows between unbounded preceding and unbounded following) as actual_delivery_date, min(gmp.ship_date) over (partition by gmp.fulfillment_shipment_id, gmp.package_id) as ship_date, FIRST_VALUE(gmp.edi_reason_code) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification IN ('DeliveryAttempted', 'DeliveryReceived') and gmp.attempted_delivery_date = gmp.event_date then gmp.attempted_delivery_date else null end rows between unbounded preceding and unbounded following) as edi_reason_code_attempt_delivery, FIRST_VALUE(gmp.edi_status_code) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification IN ('DeliveryAttempted', 'DeliveryReceived') and gmp.attempted_delivery_date = gmp.event_date then gmp.attempted_delivery_date else null end rows between unbounded preceding and unbounded following) as edi_status_code_attempt_delivery, FIRST_VALUE(gmp.edi_reason_code) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification = 'DeliveryReceived' then gmp.actual_delivery_date else null end rows between unbounded preceding and unbounded following) as edi_reason_code_actual_delivery, FIRST_VALUE(gmp.edi_status_code) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification = 'DeliveryReceived' then gmp.actual_delivery_date else null end rows between unbounded preceding and unbounded following) as edi_status_code_actual_delivery, FIRST_VALUE(gmp.tcda_container_id) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by gmp.attempted_delivery_date rows between unbounded preceding and unbounded following) as tcda_container_id, NULLIF(FIRST_VALUE(gmp.event_timezone) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification IN ('DeliveryAttempted', 'DeliveryReceived') and gmp.attempted_delivery_date = gmp.event_date then gmp.attempted_delivery_date else null end rows between unbounded preceding and unbounded following),'') as attempted_event_timezone, NULLIF(FIRST_VALUE(gmp.event_timezone) over (partition by gmp.fulfillment_shipment_id, gmp.package_id order by case when gmp.status_classification = 'DeliveryReceived' then gmp.actual_delivery_date else null end rows between unbounded preceding and unbounded following),'') as delivery_event_timezone FROM public.T_CHANGED_D_PACKAGE_ITEMS packages JOIN perfectmile.raw_opls_shipment_events_v2 gmp ON ( gmp.fulfillment_shipment_id = packages.fulfillment_shipment_id AND gmp.package_id = packages.package_id ) LEFT JOIN public.T_CHANGED_OPLS_PACKAGE_ITEMS del ON del.fulfillment_shipment_id = gmp.fulfillment_shipment_id and del.package_id = gmp.package_id where del.fulfillment_shipment_id is null and gmp.loaded_at >= to_timestamp('20210310080000','YYYYMMDDHH24MISS') - INTERVAL '60 DAYS'                                                                                                                                                                                                                                                                                                                                                             ]"
    extractTableName(q, "(from|join)[\\s]+([a-zA-Z0-9._]+)").foreach(println)

  }


  def getSparkSession(): SparkSession = {
    val spark = SparkSession.builder()
      .master("local[1]")
      .appName("SparkByExample")
      .getOrCreate();
    //    Logger.
    spark.sparkContext.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.OFF)
    Logger.getLogger("akka").setLevel(Level.OFF)
    val rootLogger = Logger.getRootLogger()
    rootLogger.setLevel(Level.ERROR)

    Logger.getLogger("org.apache.spark").setLevel(Level.OFF)
    Logger.getLogger("org.spark-project").setLevel(Level.OFF)


    val extractTableNameUdf = spark.udf.register("extractTableName", extractTableName _)
    val flatArrayUdf = spark.udf.register("flatArray", flatArray _)
    spark
  }

  def execute(query: String): JSONObject = {
    val result = new JSONObject();
    try {
      val df = getSparkSession().sql(query)
      val json = dfToJson(df, 1000)
      result.put("success", json)
    } catch {
      case e: Exception => {
        result.put("error", e.getMessage())
      }
    }
    return result
  }

  def createQMRReport(): Unit ={
    val spark = getSparkSession();
    import spark.implicits._

    val extractTableNameUdf = spark.udf.register("extractTableName", extractTableName _)
    val flatArrayUdf = spark.udf.register("flatArray", flatArray _)

    val config = RedshiftExecutor.readConfig().getJSONObject("qmr");
    val df = spark.read.parquet(config.getString("qmr_log_path"))
    val dfWithTable = df.dropDuplicates()
      .withColumn("table_used", extractTableNameUdf(col("querytxt"), lit("(from|join)[\\s]+([a-zA-Z0-9._]+)")))
      .withColumn("jobid", regexp_replace(extractTableNameUdf(col("querytxt"), lit("job_id::([0-9]+)"))(0), "(job_id::)", ""))
      .withColumn("jobrunid", regexp_replace(extractTableNameUdf(col("querytxt"), lit("jobrun_id::([0-9]+)"))(0), "(jobrun_id::)", ""))
      .withColumn("rule", trim(col("rule")))
      .withColumn("action", trim(col("action")))
      .withColumn("action_value", trim(col("action_value")))
      .withColumn("service_class_name", trim(col("service_class_name")))
      .withColumn("database", trim(col("database")))
      .withColumn("updated_query", regexp_replace(trim(col("querytxt")), "(.* job_id::[0-9]+)", ""))

    val dfAggregated = dfWithTable
      .groupBy($"jobid")
      .agg(collect_set($"rule").alias("rule_list"),
        collect_set($"jobrunid").alias("jobrunid"),
        collect_set($"table_used").alias("table_used"),
        collect_set(date_format($"starttime", "yyyy-MM-dd")).alias("starttime"))
      .withColumn("max_date", array_max($"starttime")).alias("df1")
      .orderBy(desc("max_date"), size($"rule_list").desc).drop("max_date")
      .select($"jobid", $"jobrunid".cast("string").alias("jobrunid"), flatArrayUdf($"table_used").cast("string").alias("table_used"), $"starttime".cast("string").alias("starttime"), size($"starttime").alias("occurrence"), $"rule_list".cast("string").alias("rule_list"))

    dfWithTable.createOrReplaceTempView("qmr")
    dfAggregated.createOrReplaceTempView("job_level_details")
    spark.sql("cache table qmr")
    spark.sql("cache table job_level_details")
  }

  def main(args: Array[String]): Unit = {

    val spark = getSparkSession();
    import spark.implicits._

    val extractTableNameUdf = spark.udf.register("extractTableName", extractTableName _)
    val flatArrayUdf = spark.udf.register("flatArray", flatArray _)

    //spark.udf.register("extractTableName", udf(extractTableName))
    val config = RedshiftExecutor.readConfig().getJSONObject("qmr");
    val df = spark.read.parquet(config.getString("qmr_log_path"))
    //df.select("querytxt").collect().foreach(println)
    val dfWithTable = df.dropDuplicates()
      .withColumn("table_used", extractTableNameUdf(col("querytxt"), lit("(from|join)[\\s]+([a-zA-Z0-9._]+)")))
      .withColumn("jobid", regexp_replace(extractTableNameUdf(col("querytxt"), lit("job_id::([0-9]+)"))(0), "(job_id::)", ""))
      .withColumn("jobrunid", regexp_replace(extractTableNameUdf(col("querytxt"), lit("jobrun_id::([0-9]+)"))(0), "(jobrun_id::)", ""))
      .withColumn("rule", trim(col("rule")))
      .withColumn("action", trim(col("action")))
      .withColumn("action_value", trim(col("action_value")))
      .withColumn("service_class_name", trim(col("service_class_name")))
      .withColumn("database", trim(col("database")))
      .withColumn("updated_query", regexp_replace(trim(col("querytxt")), "(.* job_id::[0-9]+)", ""))
    //.filter("service_class_name != 'ETLM'")


    //dfWithTable.drop("querytxt").show(false)

    //    val dfAggregated = dfWithTable.groupBy($"jobid", $"jobrunid", $"table_used", $"query", $"querytxt").agg(collect_set($"rule").alias("rule_list")).orderBy(size($"rule_list").desc, desc("jobrunid"))
    //      .select($"jobid", $"jobrunid", $"table_used".cast("string").alias("table_used"), $"query",  $"rule_list".cast("string").alias("rule_list"))
    //dfAggregated.filter("jobid = 'job_id::15334506'").show(false)
    //trim($"querytxt").as("querytxt"),
    //    dfAggregated.select("rule_list","*").show(100,false)
    //dfWithTable.filter("jobid = 'job_id::15334506'").select("querytxt").collect().foreach(println)

    //    val job_occurrence = dfWithTable.groupBy("jobid").agg(count("*").alias("job_occurrence"));

    //      .groupBy($"jobid", $"table_used", $"updated_query")
    val dfAggregated = dfWithTable //.filter("trim(jobid) != 'job_id::15334506'")
      .groupBy($"jobid")
      .agg(collect_set($"rule").alias("rule_list"),
        collect_set($"jobrunid").alias("jobrunid"),
        collect_set($"table_used").alias("table_used"),
        collect_set(date_format($"starttime", "yyyy-MM-dd")).alias("starttime"))
      .withColumn("max_date", array_max($"starttime")).alias("df1")
      .orderBy(desc("max_date"), size($"rule_list").desc).drop("max_date")
      .select($"jobid", $"jobrunid".cast("string").alias("jobrunid"), flatArrayUdf($"table_used").cast("string").alias("table_used"), $"starttime".cast("string").alias("starttime"), size($"starttime").alias("occurrence"), $"rule_list".cast("string").alias("rule_list"))

    println("----------------------")


    println("dataList as json: " + dfToJson(dfWithTable.filter("jobid in (14647025,18123830,17979008,19440963,18098360,16349881,18025988,14304202,14329794,20353464)").select("jobid", "updated_query"), 100))
    //println("dataList as json: " + dfToJson(dfAggregated,100))
    //    df.printSchema()
    println("----------------------")

    println("dfAggregated: " + dfAggregated.count())

    print("")
    println("starttime: " + dfWithTable.select(max($"starttime")).collect()(0))

    println("")
  }


  def dfToJson(df: org.apache.spark.sql.DataFrame, limit: Integer): JSONArray = {
    val dataList = df.rdd.map(r => {
      var jm = new util.HashMap[String, String]();
      for (i <- 0 to r.schema.fieldNames.length - 1) jm.put(r.schema.fieldNames(i), (r(i) + ""));
      jm
    }).take(limit)
    return new JSONArray(dataList);
  }


}

//schema.table.col.name

//