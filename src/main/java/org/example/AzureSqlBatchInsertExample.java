package org.example;

import com.microsoft.sqlserver.jdbc.SQLServerBulkCSVFileRecord;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopy;
import com.microsoft.sqlserver.jdbc.SQLServerBulkCopyOptions;
import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AzureSqlBatchInsertExample {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        // Define your Azure SQL Database connection details
        Connection connection = null;// Not showing connection details for security reasons

// Establish the connection
        try {
            //executeInsert(connection);
            bulkInsert(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

    private static void executeInsert(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO moneytable (smallmoneycol, moneycol) VALUES (?, ?)");
        // Sample data for batch insert
        double[][] employeeData = {
                {214798.3698, 922337203685487.5887},
                {-214787.3687, -922337203685987.5807}
                // Add more records as needed
        };

        // Add batch entries
        for (double[] data : employeeData) {
            preparedStatement.setDouble(1, data[0]);
            preparedStatement.setDouble(2, data[1]);
            preparedStatement.addBatch();
        }
        // Execute the batch insert
        int[] rowsAffected = preparedStatement.executeBatch();

        System.out.println("Total rows affected: " + rowsAffected.length);
    }

    private static void bulkInsert(Connection connection) throws SQLServerException {
        try {
            // Create some data to insert into our database table
            Map<Object, Object> data = new HashMap();
            data.put(214799.3698, 922337203685487.5887);
            data.put(214799.3698, 922337203685487.5887);

            // We are going to build a CSV document to use for the bulk insert
            StringBuilder stringBuilder = new StringBuilder();

            // Add table column names to CSV
            stringBuilder.append("smallmoneycol, moneycol\n");

            // Copy data from map and append to CSV
            for (Map.Entry entry : data.entrySet()) {
                stringBuilder.append(
                        String.format("%s,%s\n", entry.getKey(), entry.getValue()));
            }

            byte[] bytes = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {

                // Pass in input stream and set column information
                SQLServerBulkCSVFileRecord fileRecord = new SQLServerBulkCSVFileRecord(
                        inputStream, StandardCharsets.UTF_8.name(), ",", true);

                fileRecord.addColumnMetadata(1, "smallmoneycol", 3, 10, 4);
                fileRecord.addColumnMetadata(2, "moneycol", 3, 19, 4);

                try {

                    // Set bulk insert options, for example here I am setting a batch size
                    SQLServerBulkCopyOptions copyOptions = new SQLServerBulkCopyOptions();
                    copyOptions.setBatchSize(10000);

                    // Write the CSV document to the database table
                    try (SQLServerBulkCopy bulkCopy = new SQLServerBulkCopy(connection)) {
                        bulkCopy.setBulkCopyOptions(copyOptions);
                        bulkCopy.setDestinationTableName("moneytable");
                        bulkCopy.writeToServer(fileRecord);
                    }
                } catch (SQLServerException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}