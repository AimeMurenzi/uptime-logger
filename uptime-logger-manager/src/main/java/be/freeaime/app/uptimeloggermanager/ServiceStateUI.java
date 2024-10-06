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
import javafx.scene.layout.GridPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import be.freeaime.app.base.EntryPointULM;
import be.freeaime.app.base.PropertyUtil;

import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class ServiceStateUI {
    private static class Holder {
        private static final ServiceStateUI INSTANCE = new ServiceStateUI();
    }

    public static ServiceStateUI getInstance() {
        return Holder.INSTANCE;
    }

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
        final boolean serviceIsInstalled = Installer.isServiceInstalled();
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
                final boolean serviceIsEnabled = Installer.isServiceEnabled();
                if (serviceIsEnabled) {
                    final String title = "Disable Uptime Logger Service";
                    final String header = "This will disable the Uptime Logger Service";
                    final String message = "Do you want to proceed?";
                    final boolean userConfirmsDisableService = yesNoDialogue(title, header, message);
                    if (userConfirmsDisableService) {
                        Installer.disableService();
                    }
                } else {
                    final String title = "Enable Uptime Logger Service";
                    final String header = "This will enable the Uptime Logger Service";
                    final String message = "Do you want to proceed?";
                    final boolean userConfirmsDisableService = yesNoDialogue(title, header, message);
                    if (userConfirmsDisableService) {
                        Installer.enableService();
                    }
                }

            } catch (RuntimeException e) {
                final String errorMessage[] = e.getMessage().split(",");
                showErrorDialog(errorMessage[0], errorMessage[1], errorMessage[2]);
            }
            updateEnabledUIState();
        };
    }

    private void updateEnabledUIState() {
        final boolean serviceIsEnabled = Installer.isServiceEnabled();
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
            // check if its already installed
            final boolean serviceIsInstalled = Installer.isServiceInstalled();
            if (serviceIsInstalled) {
                // ask if user want to uninstall if yes
                uninstall();
            } else {
                install();
            }
            updateInstalledUIState();
            updateEnabledUIState();
        };
    }

    private void install() {
        final boolean userCancelsInstall = !terminateAllRunningServices();
        if (userCancelsInstall) {
            return;
        }
        try {
            Installer.install();
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
                Installer.uninstallService();
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
            final List<Long> runningServicePidList = Tool.serviceRunningCheck();
            final boolean serviceIsRunning = runningServicePidList.size() != 0;
            if (serviceIsRunning) {
                final boolean wasTerminationSuccessful = terminateAllRunningServices();
                updateRunningStateUI(wasTerminationSuccessful);
            } else {
                // TODO: check if systemctl is available
                // TODO: check if service is installed
                // TODO: move isServiceInstalled to tool
                final boolean serviceIsInstalled = Installer.isServiceInstalled();
                if (serviceIsInstalled) {
                    // TODO: check if service is enabled
                    final boolean serviceIsEnabled = Installer.isServiceEnabled();
                    if (serviceIsEnabled) {
                        // TODO: start start service
                        Installer.startService();
                    }
                }
                final List<Long> runningServicePidList_ = Tool.serviceRunningCheck();
                final boolean isServiceRunning_ = runningServicePidList_.size() != 0;
                updateRunningStateUI(isServiceRunning_);
            }

        };
    }

    private void updateRunningStateUI(final boolean isServiceRunning) {
        runningStateLabel.setText(isServiceRunning ? OFF_STATE : ON_STATE);
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
