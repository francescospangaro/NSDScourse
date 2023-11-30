package it.polimi.nsds.spark.lab.friends;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.col;

/**
 * Implement an iterative algorithm that implements the transitive closure of friends
 * (people that are friends of friends of ... of my friends).
 * <p>
 * Set the value of the flag useCache to see the effects of caching.
 */
public class FriendsComputation {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "FriendsCache" : "FriendsNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("person", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("friend", DataTypes.StringType, false));
        final StructType schema = DataTypes.createStructType(fields);

        final Dataset<Row> input = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(schema)
                .csv(filePath + "files/friends/friends.csv");


        var i2 = input.as("i2");
        var i1 = input.as("i1");
        Dataset<Row> newFriends;
        var lastIteration = input;
        var mergedFriends = input;
        do {
            lastIteration = mergedFriends;
            i2 = mergedFriends.as("i2");
            i1 = mergedFriends.as("i1");
            newFriends = i1.join(i2, col("i1.friend").equalTo(col("i2.person")), "inner")
                    .select(col("i1.person").as("person"), col("i2.friend").as("friend"));
            mergedFriends = lastIteration.union(newFriends).distinct();
            mergedFriends.cache();
            lastIteration.cache();
        } while (lastIteration.count() != mergedFriends.count());


        mergedFriends.show();

        spark.close();
    }

}