package be.freeaime.app.uptimeloggermanager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FxEntryPoint extends Application { 
    @Override
    public void start(Stage primaryStage) throws Exception {   
        primaryStage.setTitle("Uptime Logger Manager");
        primaryStage.setScene(new Scene(Manager.get(), 800, 600));
        primaryStage.show(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
