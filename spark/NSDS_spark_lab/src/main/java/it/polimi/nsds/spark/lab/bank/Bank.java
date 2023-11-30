package it.polimi.nsds.spark.lab.bank;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.*;

/**
 * Bank example
 * <p>
 * Input: csv files with list of deposits and withdrawals, having the following
 * schema ("person: String, account: String, amount: Int)
 * <p>
 * Queries
 * Q1. Print the total amount of withdrawals for each person
 * Q2. Print the person with the maximum total amount of withdrawals
 * Q3. Print all the accounts with a negative balance
 * Q4. Print all accounts in descending order of balance
 * <p>
 * The code exemplifies the use of SQL primitives.  By setting the useCache variable,
 * one can see the differences when enabling/disabling cache.
 */
public class Bank {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "BankWithCache" : "BankNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> mySchemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("person", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("account", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("amount", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        final Dataset<Row> deposits = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/deposits.csv");

        final Dataset<Row> withdrawals = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/withdrawals.csv");

        // Used in two different queries
        if (useCache) {
            withdrawals.cache();
        }

        // Q1. Total amount of withdrawals for each person
        System.out.println("Total amount of withdrawals for each person");

        final Dataset<Row> totWithdrawals = withdrawals
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalWithdrawals");

        totWithdrawals.show();


        // Q2. Person with the maximum total amount of withdrawals
        System.out.println("Person with the maximum total amount of withdrawals");
        final Dataset<Row> maxWithdrawals = totWithdrawals
                .select("account")
                .where(col("sum(amount)")
                        .equalTo(totWithdrawals
                                .agg(max(col("sum(amount)")))
                                .first()
                                .getLong(0)));

        maxWithdrawals.show();

        // Q3 Accounts with negative balance
        System.out.println("Accounts with negative balance");

        final Dataset<Row> totDeposits = deposits
                .groupBy("account")
                .sum("amount")
                .as("totalDeposits")
                .drop("person");

        final Dataset<Row> negativeBalance = totWithdrawals
                .join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "left_outer")
                .where(totDeposits.col("account").isNull()
                        .or(totWithdrawals.col("sum(amount)").gt(totDeposits.col("sum(amount)"))))
                .select(totWithdrawals.col("account"));

        negativeBalance.show();

        // Q4 Accounts in descending order of balance
        System.out.println("Accounts in descending order of balance");

        final Dataset<Row> valueAccount = totWithdrawals
                .join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "full_outer")
                .select(when(totDeposits.col("account").isNull(), totWithdrawals.col("account")).otherwise(totDeposits.col("account")).as("Account"),
                        when(totDeposits.col("account").isNull(), 0).otherwise(totDeposits.col("sum(amount)"))
                                .minus(when(totWithdrawals.col("account").isNull(), 0).otherwise(totWithdrawals.col("sum(amount)"))).as("balance"))
                .orderBy(desc("balance"));

        valueAccount.show();


        spark.close();

    }
}