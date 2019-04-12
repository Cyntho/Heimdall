package org.cyntho.ts.heimdall.database;

import org.cyntho.ts.heimdall.app.Bot;
import org.cyntho.ts.heimdall.logging.LogLevelType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSetup {

    public static boolean doSetup() throws SQLException, IOException, NullPointerException {

        if (Bot.heimdall == null || Bot.heimdall.getDb() == null)
            throw new NullPointerException("Could not access database. Is the connection initialized?");


        Connection connection = Bot.heimdall.getDb().getConnection();

        String s;
        StringBuilder sb = new StringBuilder();

        try {

            // Init stream from resource
            InputStream is = DatabaseSetup.class.getResourceAsStream("tsb_main.sql");

            // create temporary file
            File tempFile = File.createTempFile("heimdall-sql", ".tmp");
            Path pathToTemp = tempFile.toPath();
            Files.copy(is, pathToTemp, StandardCopyOption.REPLACE_EXISTING);

            // init reader
            FileReader fileReader = new FileReader(tempFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // read from temp file, exclude comments (-- and /*)
            while ((s = bufferedReader.readLine()) != null){
                if (!(s.startsWith("--") || (s.startsWith("/*")))){
                    sb.append(s);
                }
            }

            bufferedReader.close();

            // split the file into single, executable sql statements
            // by using the splitter ';'
            String[] parts = sb.toString().split(";");

            Statement statement = connection.createStatement();


            // execute all parts, continue on error (but log it)
            for (String part : parts) {

                // skip lines starting or ending with spaces
                // to avoid executing empty statements
                if (!part.trim().equals("")) {
                    try {
                        statement.execute(part);
                    } catch (SQLException e) {
                        Bot.heimdall.log(LogLevelType.DATABASE_ERROR, "Could not execute setup statement: " + part);
                    }
                }
            }

            // close streams etc. and delete temp file
            try {
                tempFile.deleteOnExit();
                is.close();
                fileReader.close();
                bufferedReader.close();
            } catch (Exception ignored) {}

            return true;

        } catch (IOException e){
            throw new IOException("Error working with temp file");
        }
    }

}
