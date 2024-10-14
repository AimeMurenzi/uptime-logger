package be.freeaime.app.uptimeloggermanager;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.event.EventHandler;
import be.freeaime.app.uptimeloggermanager.services.InstallerService;

import java.util.*;

public class ServiceStateUI {
    private static class Holder {
        private static final ServiceStateUI INSTANCE = new ServiceStateUI();
    }

    public static ServiceStateUI getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * TODO:DONE implement the following
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
    private final String ON_STATE = "✔";
    private final String OFF_STATE = "✖";
    @FXML
    private Label installedStateLabel;
    @FXML
    private Label enabledStateLabel;
    @FXML
    private Label runningStateLabel;

    @FXML
    private Button installBTN;
    @FXML
    private Button enableBTN;
    @FXML
    private Button startBTN;

    private final GridPane root;

    public void showErrorDialog(String title, String header, String message) {
        showInformativeDialog(title, header, message, AlertType.ERROR);
    }

    public void showInfoDialog(String title, String header, String message) {
        showInformativeDialog(title, header, message, AlertType.INFORMATION);
    }

    private void showInformativeDialog(String title, String header, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateInstalledUIState() {
        final boolean serviceIsInstalled = InstallerService.isServiceInstalled();
        if (serviceIsInstalled) {
            this.installedStateLabel.setText(ON_STATE);
            this.installBTN.setText("Uninstall");
        } else {
            this.installedStateLabel.setText(OFF_STATE);
            this.installBTN.setText("Install");
        }
    }

    private ServiceStateUI() {
        this.root = getLoadedRoot();
        updateInstalledUIState();
        updateEnabledUIState();
        updateRunningUIState();
        final List<Long> uptimeLoggerPidList = Tool.serviceRunningCheck();
        final boolean isServiceRunning = uptimeLoggerPidList.size() != 0;
        runningStateLabel.setText(isServiceRunning ? ON_STATE : OFF_STATE);
        startBTN.setText(isServiceRunning ? "Stop" : "Start");
        installBTN.setOnAction(installBTNEventHandler());
        enableBTN.setOnAction(enableBTNEventHandler());
        startBTN.setOnAction(startBTNEventHandler());

    }

    private EventHandler<ActionEvent> enableBTNEventHandler() {
        return event -> {
            try {
                final boolean serviceIsEnabled = InstallerService.isServiceEnabled();
                if (serviceIsEnabled) {
                    final String title = "Disable Uptime Logger Service";
                    final String header = "This will disable the Uptime Logger Service";
                    final String message = "Do you want to proceed?";
                    final boolean userConfirmsDisableService = yesNoDialogue(title, header, message);
                    if (userConfirmsDisableService) {
                        InstallerService.disableService();
                    }
                } else {
                    final String title = "Enable Uptime Logger Service";
                    final String header = "This will enable the Uptime Logger Service";
                    final String message = "Do you want to proceed?";
                    final boolean userConfirmsDisableService = yesNoDialogue(title, header, message);
                    if (userConfirmsDisableService) {
                        InstallerService.enableService();
                    }
                }
            } catch (RuntimeException e) {
                final String errorMessage[] = e.getMessage().split(",");
                showErrorDialog(errorMessage[0], errorMessage[1], errorMessage[2]);
            }
            updateEnabledUIState();
            updateRunningUIState();
        };
    }

    private void updateEnabledUIState() {
        final boolean serviceIsEnabled = InstallerService.isServiceEnabled();
        if (serviceIsEnabled) {
            enableBTN.setText("Disable");
            enabledStateLabel.setText(ON_STATE);
        } else {
            enableBTN.setText("Enable");
            enabledStateLabel.setText(OFF_STATE);
        }
    }

    private EventHandler<ActionEvent> installBTNEventHandler() {
        return event -> {
            // check if it already installed
            final boolean serviceIsInstalled = InstallerService.isServiceInstalled();
            if (serviceIsInstalled) {
                // ask if user wants to uninstall, if yes uninstall
                uninstall();
            } else {
                install();
            }
            updateInstalledUIState();
            updateEnabledUIState();
            updateRunningUIState();
        };
    }

    private void install() {
        final boolean userCancelsInstall = !terminateAllRunningServices();
        if (userCancelsInstall) {
            return;
        }
        try {
            InstallerService.install();
            final String infoTitle = "Install Uptime Logger Service";
            final String infoHeader = "Service was successfully installed";
            showInfoDialog(infoTitle, infoHeader, null);
        } catch (RuntimeException re) {
            re.printStackTrace();
            showErrorDialog("Failed To Install", "Access Denied", re.getMessage());
        }
    }

    private void uninstall() {
        final String title = "Uninstall Uptime Logger Service";
        final String header = "This will uninstall the Uptime Logger Service";
        final String message = "Do you want to proceed?";
        final boolean userConfirmsUninstall = yesNoDialogue(title, header, message);
        if (userConfirmsUninstall) {
            try {
                InstallerService.uninstallService();
                final String infoTitle = "Uninstall Uptime Logger Service";
                final String infoHeader = "Service was successfully uninstalled";
                showInfoDialog(infoTitle, infoHeader, null);
            } catch (RuntimeException e) {
                final String errorMessage[] = e.getMessage().split(",");
                showErrorDialog(errorMessage[0], errorMessage[1], errorMessage[2]);
                final String errorTitle = "Uninstall Uptime Logger Service";
                final String errorHeader = "Uninstall was incomplete";
                showErrorDialog(errorTitle, errorHeader, null);
            }
        }
    }

    private EventHandler<ActionEvent> startBTNEventHandler() {
        return event -> {
            final boolean serviceIsRunning = InstallerService.isServiceRunning();
            if (serviceIsRunning) {
                final String title = "Stop Uptime Logger Service";
                final String header = "This will stop the Uptime Logger Service";
                final String message = "Do you want to proceed?";
                final boolean userConfirmsStopService = yesNoDialogue(title, header, message);
                if (userConfirmsStopService) {
                    try {
                        InstallerService.stopService();
                    } catch (RuntimeException e) {
                        final String errorMessage[] = e.getMessage().split(",");
                        showErrorDialog(errorMessage[0], errorMessage[1], errorMessage[2]);
                    }
                }
            } else {
                try {
                    InstallerService.startService();
                } catch (RuntimeException e) {
                    final String errorMessage[] = e.getMessage().split(",");
                    showErrorDialog(errorMessage[0], errorMessage[1], errorMessage[2]);
                }
            }
            updateRunningUIState();
        };
    }

    private void updateRunningUIState() {
        final boolean isServiceRunning = InstallerService.isServiceRunning();
        runningStateLabel.setText(isServiceRunning ? ON_STATE : OFF_STATE);
        startBTN.setText(isServiceRunning ? "Stop" : "Start");
    }

    private boolean terminateAllRunningServices() {
        final String title = "Closing all running Uptime Logger Service instances";
        final String header = "This will close all running instances of the Uptime Logger Service";
        final String message = "Do you want to proceed?";
        return yesNoDialogue(title, header, message);
    }

    private boolean yesNoDialogue(final String title, final String header, final String message) {
        final ButtonType yesButton = new ButtonType("Yes");
        final ButtonType noButton = new ButtonType("No");
        final boolean promptResult[] = { false };
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert
                .getButtonTypes()
                .setAll(yesButton, noButton);
        alert
                .showAndWait()
                .ifPresent(response -> {
                    if (response == yesButton) {
                        final List<Long> failedClosureList = Tool.stopAllUptimeLoggers();
                        promptResult[0] = failedClosureList.size() == 0;
                    }
                });
        return promptResult[0];
    }

    private GridPane getLoadedRoot() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/uptimeLoggerServiceStateUI.fxml"));
        GridPane loadedRoot = null;
        try {
            loader.setController(this);
            loadedRoot = loader.load();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadedRoot;
    }

    public static GridPane get() {
        return getInstance().root;
    }

}
