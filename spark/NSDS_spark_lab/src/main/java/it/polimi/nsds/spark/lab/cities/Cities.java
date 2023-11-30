package it.polimi.nsds.spark.lab.cities;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.*;

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        // TODO: add code here if necessary

        final Dataset<Row> q1 = citiesRegions
                .join(citiesPopulation, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .groupBy("region")
                .sum("population");

        q1.show();

        final Dataset<Row> q2 = citiesRegions
                .join(citiesPopulation, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .groupBy("region")
                .agg(count("*").as("Num cities"), max("population"));
        q2.show();

        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        // TODO: add code here to produce the output for query Q3

        int iteration = 0;
        int sum = sumPopulations(population);
        while (sum < 100000000) {
            iteration++;

            population = population.map(i -> {
                if (i > 1000)
                    return (int) Math.round(i * 1.01);
                else return (int) Math.round(i * 0.99);
            });

            sum = sumPopulations(population);
            population.cache();

            System.out.println("Year: " + iteration + " total population: " + sum);
        }


        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        final StreamingQuery q4 = bookings
                .join(citiesPopulation, bookings.col("value").equalTo(citiesPopulation.col("id")))
                .join(citiesRegions, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("region")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }


    private static int sumPopulations(JavaRDD<Integer> populations) {
        return populations.reduce(Integer::sum);
    }

}