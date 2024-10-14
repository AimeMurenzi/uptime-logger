package be.freeaime.app.base;

import java.io.IOException;

import be.freeaime.app.uptimeloggermanager.FxEntryPoint;
import javafx.application.Platform;

public class EntryPointULM {
    
    public static void main(String... args) throws IOException { 
        Platform.startup(() -> {});
        FxEntryPoint.main(args); 
    } 
}
