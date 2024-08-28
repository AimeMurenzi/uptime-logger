package be.freeaime.uptimeloggermanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FxEntryPoint extends Application { 
    /**
     * TODO: implement the following
     * logRecordTextArea
     * //used to replace currently being viewed
     * selectedOptionContainerVbox
     * //for selecting current being viewed, use grouping
     * recordsOptionToggle
     * serviceStateToggle
     * 
     * service state controls ✖ ✔
     * 
     * serviceInstallStateLabel
     * serviceEnableStateLabel
     * serviceRunningStateLabel
     */

   
    @Override
    public void start(Stage primaryStage) throws Exception {  
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/uptimeLoggerManager.fxml"));
        HBox root = loader.load();   

        primaryStage.setTitle("uptime logger manager");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
