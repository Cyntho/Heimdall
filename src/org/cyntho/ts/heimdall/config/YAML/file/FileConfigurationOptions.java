package org.cyntho.ts.heimdall.config.YAML.file;

import org.cyntho.ts.heimdall.config.YAML.MemoryConfiguration;
import org.cyntho.ts.heimdall.config.YAML.MemoryConfigurationOptions;

/**
 * Created by Xida on 12.07.2017.
 */
public class FileConfigurationOptions extends MemoryConfigurationOptions {
    private String header = null;
    private boolean copyHeader = true;

    protected FileConfigurationOptions(MemoryConfiguration configuration){
        super(configuration);
    }

    @Override
    public FileConfiguration configuration(){
        return (FileConfiguration) super.configuration();
    }

    @Override
    public FileConfigurationOptions copyDefaults(boolean value){
        super.copyDefaults(value);
        return this;
    }

    @Override
    public FileConfigurationOptions pathSeparator(char value){
        super.pathSeparator(value);
        return this;
    }

    public String header(){
        return header;
    }

    public FileConfigurationOptions header(String value){
        this.header = value;
        return this;
    }

    public boolean copyHeader(){
        return copyHeader;
    }

    public FileConfigurationOptions copyHeader(boolean value){
        copyHeader = value;
        return this;
    }

}