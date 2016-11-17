# ExoplanetClassification
This repository was created to gather a project on exoplanets classification, developed for the Spark's course of the Big Data program at Telecom ParisTech. 

## Context
**_bold Objective**: Build a Binary Classifier of exoplanets labeled "confirmed" or "false-positive".

**_bold Context**: Exoplanets are planets rotating around other stars than the Sun. Their study allows us to better understand how the solar system was formed, and a fraction of them could be conducive to the development of extraterrestrial life. 

They are detected in two steps:
* A *Satellite* (Kepler) observes the stars and marks those whose luminosity curve shows a "hollow", which could indicate that a planet has passed (part of the light emitted by the star being obscured by the passage of the planet). This method of "transit" allows us to define candidate exoplanets, and to deduce the characteristics that the planet would have if it really existed (distance to its star, diameter, shape of its orbit, etc.).
* It is then necessary to validate or invalidate the candidates using another more expensive method, based on measurements of radial velocities of the star. Candidates are then classified as "confirmed" or "false-positive".

<p align="center">
  <img src="https://raw.githubusercontent.com/FaresZenaidi/ExoplanetClassification/master/images/luminosity_curve.png" alt="Luminosity curve"/>
</p>

As there are about 200 billion stars in our galaxy, and therefore potentially as much (or even more) exoplanets, their detection must be automated to "scale up". The method of transits is already automatic (more than 22 million curves of luminosity recorded by Kepler), but not the confirmation of the candidate planets, hence the automatic classifier that we will build.

## Data
Data on exoplanets is public and available online (check the [link](http://exoplanetarchive.ipac.caltech.edu/index.html)). There are already 3388 exoplanets confirmed and about as many false positives, our classifier will be trained on these data. There is one exoplanet per line. The column of labels (what we are going to try to predict) is called "koi_disposition". You can retrieve the data in csv format [here](https://drive.google.com/open?id=0Bw3qAqETA6vkUWdyZ2xSVDIwRDQ). The contents of the columns of the dataset are explained [here](http://exoplanetarchive.ipac.caltech.edu/docs/API_kepcandidate_columns.html). The classifier will only use information from the brightness curves.

## Tools
* IntelliJ: IDE that allows to develop data science projects in Spark/Scala.
* Spark: Distributed Machine Leaning framework. 
* Scala: Programming language used to submit jobs to Spark. 

## Running configuration
1) Load this repository on IntelliJ

2) Build the project:
```
sbt assembly
```
3) Run the project 
* Initial spark-submit command:
```
./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp" --driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML ~/Desktop/BigData/SparkProject/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar
```
Add two arguments:
* First argument: path of the cleanedData (also present in the repository). In my case:
```
/Desktop/BigData/SparkProject/cleanedDataFrame.csv
```
* Second argument: directory to save the output model. In my case:
```
~/Desktop/BigData/SparkProject/bestModelConf
```
Final command:
```
./spark-submit --conf spark.eventLog.enabled=true --conf spark.eventLog.dir="/tmp" --driver-memory 3G --executor-memory 4G --class com.sparkProject.JobML ~/Desktop/BigData/SparkProject/tp_spark/target/scala-2.11/tp_spark-assembly-1.0.jar ~/Desktop/BigData/SparkProject/cleanedDataFrame.csv ~/Desktop/BigData/SparkProject/bestModelConf
```
## Model performance  
By running our model on the test data and generating predictions, the following confusion matrix  and accuracy score are obtained:
<center>

| Labels 	| Predictions 	| Count 	|
|--------	|-------------	|-------	|
| 1.0    	| 1.0         	| 205   	|
| 0.0    	| 1.0         	| 18    	|
| 1.0    	| 0.0         	| 10    	|
| 0.0    	| 0.0         	| 387   	|

</center>

```
Accuracy score = 0.9545219638242894
```

## Contributors 
* [Fares Zenaidi](https://github.com/FaresZenaidi)
* [Hamid Amara](https://github.com/haa99)
