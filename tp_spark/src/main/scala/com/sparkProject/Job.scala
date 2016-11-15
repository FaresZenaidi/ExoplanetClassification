package com.sparkProject

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object Job {

  def main(args: Array[String]): Unit = {

    // SparkSession configuration
    val spark = SparkSession
      .builder
      .appName("spark session TP_parisTech")
      .getOrCreate()

    val sc = spark.sparkContext

    import spark.implicits._


    /** ******************************************************************************
      *
      * TP 1
      *
      *        - Set environment, InteliJ, submit jobs to Spark
      *        - Load local unstructured data
      *        - Word count , Map Reduce
      * *******************************************************************************/

    // ----------------- word count ------------------------

    val df_wordCount = sc.textFile("/users/admin/Desktop/Installation/spark-2.0.0-bin-hadoop2.7/README.md")
      .flatMap { case (line: String) => line.split(" ") }
      .map { case (word: String) => (word, 1) }
      .reduceByKey { case (i: Int, j: Int) => i + j }
      .toDF("word", "count")

    df_wordCount.orderBy($"count".desc).show()


    /** ******************************************************************************
      *
      * TP 2 : début du projet
      *
      * *******************************************************************************/


    /** ******************************************************************************************************
      *                                      Part I - Data Loading                                           *
      * ******************************************************************************************************/

    // Creation d'une rdd pour analyser le fichier csv (header notemment)
    val rdd = sc.textFile("/users/admin/Desktop/BigData/SparkProject/raw_data/cumulative.csv")
    // Actions
    print(rdd.first()) // Header
    print(rdd.take(2)) // Takes the first 2 lines

    // Creation de la DF à l'aide d'un DataFrame reader
    val df = spark.read // returns a DataFrameReader, giving access to methods “options” and “csv”
                  .option("header", "true") // Use first line of all files as header
                  .option("inferSchema", "true") // Automatically infer data types
                  .option("comment", "#") // All lines starting with # are ignored
                  .csv("/Users/admin/Desktop/BigData/SparkProject/raw_data/cumulative.csv")

    // df.printSchema()  // prints out the schema, the data types and whether a column can be null.

    println("BEFORE CLEANING - NUMBER OF COLUMNS: ", df.columns.length) // df.columns returns an Array of columns,

    println("BEFORE CLEANING- NUMBER OF ROWS: ", df.count())

    // df.show()  // Prints the DF as a table

    val columns = df.columns.slice(10, 20) // df.columns returns an Array. In Scala, arrays have a method “slice” returning a slice of the array

    df.select(columns.map(col): _*).show(50) //

    df.groupBy($"koi_disposition").count().orderBy($"count".desc).show()

    df.select($"koi_disposition").distinct().show() // Afficher les valeurs distinctes de koi_disposition

    /** ******************************************************************************************************
      *                                    Part II- Data Cleaning                                            *
      * ******************************************************************************************************/

    val cdf = df.where("koi_disposition = 'CONFIRMED' or koi_disposition = 'FALSE POSITIVE'")

    // val cdf = df.filter($"koi_disposition" === "CONFIRMED" || $"koi_disposition" === "FALSE POSITIVE")


    println("LE NOMBRE D'ELEMENTS DISTINCTS DANS koi_eccen_err1 EST: " + df.select("koi_eccen_err1").distinct().count()) // Nombre d'elements distincts dans la colonne "koi_eccen_err1"

    val cdf1 = cdf.drop("koi_eccen_err1")

    val cdf2 = cdf1.drop("index", "kepid", "koi_fpflag_nt", "koi_fpflag_ss", "koi_fpflag_co",
                        "koi_fpflag_ec", "koi_sparprov", "koi_trans_mod", "koi_datalink_dvr",
                        "koi_datalink_dvs", "koi_tce_delivname", "koi_parm_prov", "koi_limbdark_mod",
                        "koi_fittype", "koi_disp_prov", "koi_comment", "kepoi_name", "kepler_name",
                        "koi_vet_date", "koi_pdisposition")

    println("AFTER CLEANING - NUMBER OF COLUMNS: ", cdf2.columns.length) // df.columns returns an Array of columns,

    println("AFTER CLEANING - NUMBER OF ROWS: ", cdf2.count())

    // Détermination des colonnes qui ne contiennent qu'une seule valeur (drop 24 columns)
    cdf2.persist
    val useless_column = cdf2.columns.filter{case (column:String) =>
                         cdf2.agg(countDistinct(column)).first().getLong(0) <= 1 }

    val cdf3 = cdf2.drop(useless_column:_*)

    cdf3.describe("koi_impact", "koi_duration").show()

    val cdf4 = cdf3.na.fill(0.0)  // Remplacer les valeurs manquantes par 0

    // Joindre deux DF
    val df_labels = cdf4.select("rowid", "koi_disposition")

    val df_features = cdf4.drop("koi_disposition")

    val df_joined = df_features.join(df_labels, usingColumn = "rowid")

    // Manipulaiton de colonnes
    def udf_sum = udf((col1: Double, col2: Double) => col1 + col2)

    val df_newFeatures = cdf4.withColumn("koi_ror_min", udf_sum($"koi_ror", $"koi_ror_err2"))
                             .withColumn("koi_ror_max", $"koi_ror" + $"koi_ror_err1")

    df_newFeatures.show()
    df_newFeatures.coalesce(1)
                  .write
                  .mode("overwrite")
                  .option("header", "true")
                  .csv("/Users/admin/Desktop/BigData/SparkProject/tp_spark/cleanedDataFrame.csv")


  }
}