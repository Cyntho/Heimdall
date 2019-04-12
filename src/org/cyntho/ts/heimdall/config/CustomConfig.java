package org.cyntho.ts.heimdall.config;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.cyntho.ts.heimdall.config.YAML.file.FileConfiguration;
import org.cyntho.ts.heimdall.config.YAML.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.cyntho.ts.heimdall.util.NumberConversations.*;

public abstract class CustomConfig {


    private volatile FileConfiguration fileConfiguration;
    protected final boolean initialized;


    @Nullable private String internPath;
    @NotNull  private String externalPath;


    /* Constructor */
    public CustomConfig(@Nullable String intern, @NotNull String external) throws IOException {
        internPath = intern;
        externalPath = external;
        fileConfiguration = createExternal();
        initialized = fileConfiguration != null;
    }

    /* Helper */
    private boolean hasDefault(){
        return internPath != null;
    }


    /* Create external file  */
    protected FileConfiguration createExternal() throws IOException{
        if (hasDefault()){
            File internal  = new File(internPath);

            InputStream is = getClass().getResourceAsStream(internPath);
            Path target = Paths.get(externalPath);

            if (!internal.exists() && !internal.isDirectory()){
                Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            File external = new File(externalPath);
            if (!external.exists() && !external.isDirectory()){
                throw new IOException("Could not create config file at: " + externalPath);
            }
        }
        return YamlConfiguration.loadConfiguration(new File(externalPath));
    }

    @Nullable final FileConfiguration getFileConfiguration() {
        return this.fileConfiguration;
    }


    /* GETTER for config values */

    @NotNull public final Object get(String path, Object def){
        if (fileConfiguration != null){
            return fileConfiguration.get(path, def);
        } else {
            return def;
        }
    }

    @NotNull public final String getString(String path, String def){
        return fileConfiguration.getString(path, def);
    }

    @NotNull public final List<String> getStringList(String path) { return fileConfiguration.getStringList(path); }

    @NotNull public final int getInt(String path, int def){
        return toInt(get(path, def));
    }

    @NotNull public final float getFloat(String path, float def){
        return toFloat(get(path, def));
    }

    @NotNull public final double getDouble(String path, double def){
        return toDouble(get(path, def));
    }

    @NotNull public final long getLong(String path, long def){
        return toLong(get(path, def));
    }

    @NotNull public final short getShort(String path, short def){
        return toShort(get(path, def));
    }

    @NotNull public final byte getByte(String path, byte def){
        return toByte(get(path, def));
    }

    @NotNull public final boolean getBoolean(String path, boolean def) { return (boolean) get(path, def);}









}
