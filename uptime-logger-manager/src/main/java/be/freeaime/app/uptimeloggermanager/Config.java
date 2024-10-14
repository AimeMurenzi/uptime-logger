package be.freeaime.app.uptimeloggermanager;

import be.freeaime.app.base.PropertyUtil;

public class Config {

    public static final String APP_NAME =PropertyUtil.getProperty("app.parent.artifactId"); 
    public static final String SERVICE_NAME = PropertyUtil.getProperty("app.uptimeServiceName") ; 
    public static final String SERVICE_JAR_NAME=SERVICE_NAME+ ".jar";
    public static final String SERVICE_JAR_DST_DIR = String.format("/opt/be/freeaime/%s",APP_NAME);
    public static final String SERVICE_INSTALL_DST_DIR = "/lib/systemd/system"; 
    public static final String SERVICE_INSTALL_NAME = String.format("%s.service",APP_NAME); 
    public static final String SERVICE_LOGGER_FILE_NAME = "uptime.csv"; 
}
