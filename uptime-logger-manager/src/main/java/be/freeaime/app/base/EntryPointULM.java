package be.freeaime.app.base;

import java.io.IOException;

import be.freeaime.app.uptimeloggermanager.FxEntryPoint;
import javafx.application.Platform;

public class EntryPointULM {
    
    public static void main(String... args) throws IOException { 

        System.out.println("Hello Wall");
        final String USERNAME= System.getProperty("user.name");
        final String SUDO_USER = System.getenv("SUDO_USER");
        System.out.println("USERNAME:  "+USERNAME);
        System.out.println("SUDO_USER: " +SUDO_USER);
        Platform.startup(() -> {});
        FxEntryPoint.main(args);


        // final String version = PropertyUtil.getProperty("app.parent.version");
        // final String title = PropertyUtil.getProperty("app.name");
        // final String serviceName = PropertyUtil.getProperty("app.uptimeServiceName")+".jar";
        // if (title != null && version != null) {
        //     System.out.println(Manager.ruler);
        //     System.out.println(title.toUpperCase());
        //     System.out.println("[VERSION      ] " + version);
        //     System.out.println("[service name ] ".toUpperCase()+ serviceName);
        //     System.out.println(Manager.ruler);
        // }

        // URL jarUrl = EntryPointULM.class.getResource("/services/"+serviceName);

        // if (jarUrl != null) {
        //     System.out.println("JAR file found at: " + jarUrl.getPath());
        //     // You can load the JAR file and use it as needed
        // } else {
        //     System.out.println("JAR file not found in resources!");
        // }
    } 
}
