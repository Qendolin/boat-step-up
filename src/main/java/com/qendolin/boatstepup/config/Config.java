package com.qendolin.boatstepup.config;

public interface Config {
    String getId();

    /**
     * @return The file name and path without the extension
     */
    default String getFileName() {
        return this.getId();
    }
    int getVersion();
}
