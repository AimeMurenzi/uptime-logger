package be.freeaime.app.uptimeloggermanager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

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



        primaryStage.setTitle("uptime logger manager");
        primaryStage.setScene(new Scene(Manager.get(), 800, 600));
        primaryStage.show(); 
    }

    public static void main(String[] args) {
        launch(args);
    }
}
