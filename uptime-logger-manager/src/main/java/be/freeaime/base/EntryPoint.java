package be.freeaime.base;

import java.io.IOException;

import be.freeaime.uptimeloggermanager.FxEntryPoint;
import javafx.application.Platform;

public class EntryPoint {
    
    public static void main(String... args) throws IOException { 
        System.out.println("Hello Wall");
          Platform.startup(() -> {});
          FxEntryPoint.main(args);
    } 
}
