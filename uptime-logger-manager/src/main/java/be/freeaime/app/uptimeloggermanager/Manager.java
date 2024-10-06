package be.freeaime.app.uptimeloggermanager;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Manager {
    private static class Holder {
        private static final Manager INSTANCE = new Manager();
    }

    public static Manager getInstance() {
        return Holder.INSTANCE;
    }

    private final HBox root;
    @FXML
    private ToggleButton recordsOptionToggle;
    @FXML
    private ToggleButton serviceStateToggle;
    @FXML
    private TextArea logRecordTextArea;
    @FXML
    private VBox selectedOptionContainerVbox;

    private final GridPane serviceStateUI = ServiceStateUI.get();
    private final ToggleGroup optionToggleGroup = new ToggleGroup();
    public static final String ruler = "════════════════════════════════════════════════════════════════════════";
    private Manager() {
        this.root = getLoadedRoot();
        serviceStateToggle.setToggleGroup(optionToggleGroup);
        recordsOptionToggle.setToggleGroup(optionToggleGroup);
        final String serviceStateToggleSelected = "serviceStateToggle";
        final String recordsOptionToggleSelected = "recordsOptionToggle";
        serviceStateToggle.setUserData(serviceStateToggleSelected);
        recordsOptionToggle.setUserData(recordsOptionToggleSelected);

        optionToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ToggleButton selectedButton = (ToggleButton) newValue;
                final String selected = (String) selectedButton.getUserData();
                switch (selected) {
                    case serviceStateToggleSelected:
                        selectedOptionContainerVbox.getChildren().remove(logRecordTextArea);
                        selectedOptionContainerVbox.getChildren().add(serviceStateUI);
                        break;
                    case recordsOptionToggleSelected:
                        selectedOptionContainerVbox.getChildren().remove(serviceStateUI);
                        selectedOptionContainerVbox.getChildren().add(logRecordTextArea);
                        break;
                    default:
                        break;
                }
            }
        });

        serviceStateToggle.setSelected(true);
        logRecordTextArea.setText("2024-08-28 # 0002Days # 09H:34M \n" + //
                        "2024-08-28 # 0002Days # 09H:34M \n" + //
                        "2024-08-28 # 0002Days # 09H:34M \n" + //
                        "2024-08-28 # 0002Days # 09H:34M \n" );

    }

    private HBox getLoadedRoot() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/uptimeLoggerManager.fxml"));
        HBox loadedRoot = null;
        try {
            loader.setController(this);
            loadedRoot = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedRoot;
    }

    public static HBox get() {
        return getInstance().root;
    }
}
