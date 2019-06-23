package org.cyntho.ts.heimdall.logging;

import org.cyntho.ts.heimdall.app.Bot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class LogArchive {


    public int archiveAll(){

        // check environment
        File dir = new File(System.getProperty("user.dir") + "/logs");
        if (!dir.exists() || !dir.isDirectory()){
            Bot.heimdall.log(LogLevelType.BOT_CRITICAL, "Could not archive older log files. Directory doesn't exist.");
            return -1;
        }

        // list all log files
        File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith("_heimdall.log"));
        int counter = 0;

        /*
        Iterate over files, create archive, add old content to new zip (compressed?), delete old file
         */
        for (File f : files){

            // Skip currently used log file
            if (!(f.getName().equalsIgnoreCase(Bot.logger.getCurrentLogFile()))){

                try {

                    File out = new File(System.getProperty("user.dir") + "/logs/" + f.getName().replace(".log", ".zip"));

                    FileOutputStream fos = new FileOutputStream(out);
                    ZipOutputStream zos = new ZipOutputStream(fos);

                    zos.putNextEntry(new ZipEntry(f.getName()));

                    byte[] bytes = Files.readAllBytes(Paths.get(f.getPath()));
                    zos.write(bytes, 0, bytes.length);
                    zos.closeEntry();
                    zos.close();

                    // Delete old .log file
                    if (!f.delete()){
                        System.out.println("Couldn't delete old .log file: " + f.getName());
                    } else {
                        counter++;
                    }

                } catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
        return counter;
    }


}
