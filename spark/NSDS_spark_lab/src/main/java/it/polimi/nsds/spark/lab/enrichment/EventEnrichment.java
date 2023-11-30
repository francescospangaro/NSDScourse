package it.polimi.nsds.spark.lab.enrichment;

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

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

/**
 * This code snippet exemplifies a typical scenario in event processing: merging
 * incoming events with some background knowledge.
 * <p>
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 * <p>
 * We want to count the number of products of each class in the stream. To do so, we
 * need to integrate static knowledge (product classification) and streaming data
 * (occurrences of products in the stream).
 */
public class EventEnrichment {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        final String filePath = args.length > 3 ? args[3] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("EventEnrichment")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        final Dataset<Row> inStream = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 1)
                .load();

        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");
        inStream.withWatermark("timestamp", "1 hour");

        // TODO
        final StreamingQuery query = inStream
                .join(productsClassification, inStream.col("value").equalTo(productsClassification.col("product")))
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("classification")
                )
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}