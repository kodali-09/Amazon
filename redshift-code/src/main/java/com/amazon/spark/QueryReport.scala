package com.amazon.spark

import com.amazon.clusters.RedshiftExecutor
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.json.{JSONArray, JSONObject}

import java.util
import scala.collection.mutable

object QueryReport {


  def extractTableName(query: String = "", regex: String): Array[String] = {
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

  def main(args: Array[String]): Unit = {

    val spark = getSparkSession();
    import spark.implicits._

    val extractTableNameUdf = spark.udf.register("extractTableName", extractTableName _)
    val flatArrayUdf = spark.udf.register("flatArray", flatArray _)

//
//    val df_table_info1 =  spark.read.parquet("/Users/pwbha/Documents/project/complete/src/main/resources/query_history/photon/old_photon_na_table_stats.parquet")
//    print(df_table_info1.count())
//    val df_table_info2 =  spark.read.parquet("/Users/pwbha/Documents/project/complete/src/main/resources/query_history/photon/old_photon_eu_table_stats.parquet")
//    print(df_table_info2.count())


    //spark.udf.register("extractTableName", udf(extractTableName))
    val config = RedshiftExecutor.readConfig().getJSONObject("qmr");
    val df = spark.read.parquet("/Users/pwbha/Documents/project/complete/src/main/resources/query_history/photon/photon_eu_query_logs.parquet")
    //spark.read.parquet(config.getString("query_history_path"))
    val df_table_info =  spark.read.parquet("/Users/pwbha/Documents/project/complete/src/main/resources/query_history/photon/photon_eu_table_stats.parquet")
      //spark.read.parquet(config.getString("table_info"))
      .withColumn("schematable", concat(lower($"schema"), lit("."), lower($"table")))
    //|username|schema|table|query_id|text|

    val dfWithTable = df.dropDuplicates()
      .withColumn("table_used", extractTableNameUdf(coalesce(col("text"), lit("")), lit("(from|join|,)[\\s]+([a-zA-Z0-9._]+)")))
      .withColumn("jobid", regexp_replace(extractTableNameUdf(coalesce(col("text"), lit("")), lit("job_id::([0-9]+)"))(0), "(job_id::)", ""))
      .withColumn("jobrunid", regexp_replace(extractTableNameUdf(coalesce(col("text"), lit("")), lit("jobrun_id::([0-9]+)"))(0), "(jobrun_id::)", ""))

    val df_load_tables = dfWithTable
      .filter("table is not null")
      .groupBy("schema", "table")
      .agg(collect_set("jobid").alias("jobid"))
      .select($"schema", $"table", $"jobid".cast("string").alias("jobid"))

    //println("df_load_tables: "+df_load_tables.count())
    //println("data:" + dfToJson(df_load_tables,10000))

    val df_all_tables = dfWithTable.withColumn("all_tables", explode($"table_used"))
      .filter("all_tables like '%.%'")
      .withColumn("table", trim(regexp_replace($"all_tables", "(from|join|,)[\\s]+", "")))
      .select("jobid", "table")
    // println(df_all_tables.count())

    val df_valid_tables = df_all_tables.alias("df_1")
      .join(df_table_info.alias("df_2"), col("df_1.table") === col("df_2.schematable"), "inner")
      .select("df_1.*")
      .groupBy("table")
      .agg(collect_set("jobid").alias("jobid"))
      .withColumn("schema", split($"table", "\\.")(0))
      .withColumn("table", split($"table", "\\.")(1))
      .select($"schema", $"table", $"jobid".cast("string").alias("jobid"))

    val tables_in_use = df_valid_tables.unionAll(df_load_tables).dropDuplicates()

    val table_job_info = df_table_info.alias("df_table_info")
      .join(tables_in_use.alias("tables_in_use"),
        (col("df_table_info.table") === col("tables_in_use.table")) &&
          (col("df_table_info.schema") === col("tables_in_use.schema")), "left")
      .select($"df_table_info.*", $"tables_in_use.jobid")



    df_all_tables
    //df_valid_tables.show(false)
    //    df_all_tables.show(false)
    //    df_table_info.show(false)
    //println("data:" + dfToJson(df_valid_tables, 10000))

  //  println(df_valid_tables.count()) // 194
//    println(tables_in_use.count()) // 313
    println("data:" + dfToJson(table_job_info, 10000))

 //   println(table_job_info.count())


    print("completed")
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