package com.sparkProject
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.StandardScaler
import org.apache.spark.ml.feature.StringIndexer
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator

/**
  * Created by Zenaidi Fares on 27/10/2016.
  */

// Command: ./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp" --driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML ~/Desktop/BigData/SparkProject/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar ~/Desktop/BigData/SparkProject/cleanedDataFrame.csv ~/Desktop/BigData/SparkProject/bestModelConf
object JobML {
  def main(args: Array[String]): Unit = {

    // SparkSession configuration
    val spark = SparkSession.builder
                            .appName("spark session TP_parisTech")
                            .getOrCreate()


    import spark.implicits._

    val sc = spark.sparkContext

    // Initializing paths
    var cleanedDataFramePath = ""
    var bestModelPath = ""

    // Paths determination
    if (args.length != 2) {
      println("Missing arguments. Give as first argument the path to the cleanedDataFrame you want to use for analytics" +
              "and as second argument the path of the directory in which you want to save your model.")
      sys.exit(0)
    }
    else {
      cleanedDataFramePath = args(0)
      bestModelPath = args(1)

    }

    /** ******************************************************************************************************
      *                                 Part I- Data pre-processing                                          *
      * ******************************************************************************************************/
    // Load of the cleanedDataFrame in df, using a DataFrame reader
    val df = spark.read
                  .option("header", "true")
                  .option("inferSchema", "true")
                  .csv(cleanedDataFramePath)

    // Drop the rowid column from df (not useful for our model)
    df.drop("rowid")

    println("-- Vector Assembler Form")
    val assembler = new VectorAssembler().setInputCols(df.columns.diff(Array("koi_disposition")))
                                         .setOutputCol("features")

    val dfAssembled = assembler.transform(df).select("features", "koi_disposition")
    dfAssembled.show(5)

    println("-- Centering and reduction")
    val scaler = new StandardScaler()
                    .setInputCol("features")
                    .setOutputCol("scaledFeatures")
                    .setWithStd(true)  // normalize each feature to have unit standard deviation.
                    .setWithMean(false)

    val scalerModel = scaler.fit(dfAssembled)
    val scaledDF = scalerModel.transform(dfAssembled).select("scaledFeatures", "koi_disposition")
    scaledDF.show(5)

    println("-- Indexing")
    // String indexer on koi_disposition column
    val indexer = new StringIndexer()
                      .setInputCol("koi_disposition")
                      .setOutputCol("labels")

    val indexedDF = indexer.fit(scaledDF).transform(scaledDF).select("scaledFeatures", "labels")
    indexedDF.show(5)

    /** ******************************************************************************************************
      *                                 Part II- Data Modeling                                               *
      * ******************************************************************************************************/
    // (90, 10) splitting of all data to generate training and test sets
    val Array(trainingData, testData) = indexedDF.randomSplit(Array(0.9, 0.1))

    // Logistic Regression model initialization
    val lrModel = new LogisticRegression()
                .setElasticNetParam(1.0) // L1-norm regularization: LASSO
                .setFeaturesCol("scaledFeatures")
                .setLabelCol("labels")
                .setStandardization(true)
                .setFitIntercept(true) // we want an affine regression (with false, it is a linear regression)
                .setTol(1.0e-5) // stop criterion of the algorithm based on its convergence
                .setMaxIter(300) // a security stop criterion to avoid infinite loops

    // Alpha grid creation (possible values of alpha parameter for L1 regularization)
    val array = -6.0 to (0.0, 0.5) toArray
    val arrayLog = array.map(x => math.pow(10, x))
    val paramGrid = new ParamGridBuilder().addGrid(lrModel.regParam, arrayLog).build()

    // Evaluator type: BinaryClassifier
    val eval = new BinaryClassificationEvaluator().setLabelCol("labels")

    // (70, 30) splitting of initial training set: training and validation sets generation.
    val validationSplit = new TrainValidationSplit()
                              .setEstimator(lrModel)
                              .setEvaluator(eval)
                              .setEstimatorParamMaps(paramGrid)
                              .setTrainRatio(0.7)

    // Tuning of the model
    println("-- Model tuning to find best hyper-parameter value")
    val model = validationSplit.fit(trainingData)

    // Predictions on test data
    val testResult = model.transform(testData).select("scaledFeatures", "labels", "prediction")

    // Prediction results
    println("-- Model performance on test set")
    testResult.groupBy("labels", "prediction").count.show()  // Confusion matrix
    val evaluator = eval.setRawPredictionCol("prediction")
    println("Accuracy score = " + (evaluator.evaluate(testResult)))

    // Saving the model in directory specified in the second argument of the main
    model.write.overwrite().save(bestModelPath)

  }
}