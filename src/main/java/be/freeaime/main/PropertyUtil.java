package be.freeaime.main;

import java.io.InputStream;
import java.util.Properties;

public class PropertyUtil {
    private final Properties properties;

    private PropertyUtil() {
        this.properties = new Properties();
        try (final InputStream propertiesInputStream = getClass().getClassLoader()
                .getResourceAsStream("MainApp.properties")) {
                    if (propertiesInputStream!=null) {
                        this.properties.load(propertiesInputStream); 
                    } 
        } catch (Exception e) { 
        } 
    }
    public static String getProperty(String propertyName) {
        return getInstance().properties.getProperty(propertyName);
    }

    private static class Holder {
        private static final PropertyUtil INSTANCE = new PropertyUtil();
    }

    public static PropertyUtil getInstance() {
        return Holder.INSTANCE;
    }
}
