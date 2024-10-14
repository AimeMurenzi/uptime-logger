package be.freeaime.app.uptimeloggermanager;

import java.io.IOException;

import be.freeaime.app.uptimeloggermanager.services.RecordService;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
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
    private final String serviceStateToggleSelected = "serviceStateToggle";
    private final String recordsOptionToggleSelected = "recordsOptionToggle";

    private Manager() {
        this.root = getLoadedRoot();
        serviceStateToggle.setToggleGroup(optionToggleGroup);
        recordsOptionToggle.setToggleGroup(optionToggleGroup);

        serviceStateToggle.setUserData(serviceStateToggleSelected);
        recordsOptionToggle.setUserData(recordsOptionToggleSelected);

        optionToggleGroup.selectedToggleProperty().addListener(getOptionToggleGroupEventHandler());

        serviceStateToggle.setSelected(true);

        logRecordTextArea.setText(RecordService.getRecords());
    }

    private ChangeListener<? super Toggle> getOptionToggleGroupEventHandler() {
        return (observable, oldValue, newValue) -> {
            if (newValue != null) {
                ToggleButton selectedButton = (ToggleButton) newValue;
                final String selected = (String) selectedButton.getUserData();
                switch (selected) {
                    case serviceStateToggleSelected:
                        selectedOptionContainerVbox.getChildren().remove(logRecordTextArea);
                        selectedOptionContainerVbox.getChildren().add(serviceStateUI);
                        break;
                    case recordsOptionToggleSelected:
                        logRecordTextArea.setText(RecordService.getRecords());
                        selectedOptionContainerVbox.getChildren().remove(serviceStateUI);
                        selectedOptionContainerVbox.getChildren().add(logRecordTextArea);
                        break;
                    default:
                        break;
                }
            }
        };
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
