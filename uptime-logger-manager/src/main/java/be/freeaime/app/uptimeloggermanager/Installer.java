package be.freeaime.app.uptimeloggermanager;

import java.io.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
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
import java.nio.file.*;

import java.util.*;

public class Installer {
    private class Holder {
        private static Installer INSTANCE = new Installer();
    }

    public static Installer getInstance() {
        return Holder.INSTANCE;
    }

    private static final String serviceName = Config.SERVICE_INSTALL_NAME;
    private static final String SERVICE_JAR_NAME = Config.SERVICE_JAR_NAME;
    private static final Path SERVICE_JAR_DST_FILE_PATH = Paths.get(Config.SERVICE_JAR_DST_DIR, SERVICE_JAR_NAME);
    private static final Path SERVICE_INSTALL_DST_FILE_PATH = Paths.get(Config.SERVICE_INSTALL_DST_DIR,
            Config.SERVICE_INSTALL_NAME);

    private Installer() {
        /**
         * assuming the install will be on debian or system with the same system file
         * structure
         * 
         * 
         */
        // TODO: check if service is already installed
        // TODO: if not install
        //
    }

    public static void install() { 
        sudoUserCheck();
        // TODO first stop then disable service
        Tool.stopAllUptimeLoggers();
        // copy uptime service to proper location /opt/uptime-logger/
        copyServiceJarToInstallDestination();
        // final String USERNAME= System.getProperty("user.name");
        final String SUDO_USER = System.getenv("SUDO_USER");

        // TODO:DONE change the owner of the service directory to    current user
        final boolean changingOwnerFailed = !executeCommand(
                String.format("sudo chown %s:%s -R %s", SUDO_USER, SUDO_USER, Config.SERVICE_JAR_DST_DIR));
        if (changingOwnerFailed) {
            final String title = "Authorization Issue";
            final String header = String.format("Failed to change the owner of service directory to current user(%s)",
                    SUDO_USER);
            final String errorMessage = String.format("%s,%s,%s", title, header,"");
            System.out.println(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        final StringBuilder serviceConfig = new StringBuilder();
        serviceConfig.append("[Unit]");
        serviceConfig.append("\nDescription=Uptime Logging Service");
        serviceConfig.append("\nAfter=multi-user.target");
        serviceConfig.append("\n");
        serviceConfig.append("\n[Service]");
        serviceConfig.append(String.format("\nUser=%s", SUDO_USER));
        serviceConfig.append(String.format("\nExecStart=/usr/bin/java -jar %s", SERVICE_JAR_DST_FILE_PATH.toString()));
        serviceConfig.append("\nSuccessExitStatus=143");
        serviceConfig.append("\nRestart=on-failure");
        serviceConfig.append("\nRestartSec=10");
        serviceConfig.append("\n");
        serviceConfig.append("\n[Install]");
        serviceConfig.append("\nWantedBy=multi-user.target");
        try {
            Tool.saveToTextFile(serviceConfig.toString(), SERVICE_INSTALL_DST_FILE_PATH.toString());
        } catch (AccessDeniedException | FileNotFoundException ade) {
            final String errorMessageString = "Your user does not have permission create system services. Please run the app using a user that has permission create system wide services(sudo)";
            System.out.println(errorMessageString);
            throw new RuntimeException(errorMessageString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * [Unit]
         * Description=Uptime Logging Service
         * After=multi-user.target
         * 
         * [Service]
         * User=dev
         * ExecStart=/usr/bin/java -jar /opt/be/freeaime/uptime-logger-service.jar
         * SuccessExitStatus=143
         * Restart=on-failure
         * RestartSec=10
         * 
         * [Install]
         * WantedBy=multi-user.target
         */
    }

    private static boolean copyServiceJarToInstallDestination() {
        final String SERVICE_JAR_DST_DIR_STRING = Config.SERVICE_JAR_DST_DIR;

        // where uptime logger runnable jar is located in the jar
        final String SERVICE_JAR_SRC_STRING = "/services/" + SERVICE_JAR_NAME;
        // install directory path
        final Path SERVICE_JAR_DST_DIR_PATH = Paths.get(SERVICE_JAR_DST_DIR_STRING);
        // final destination path of where the service jar will be placed

        try {
            Files.createDirectories(SERVICE_JAR_DST_DIR_PATH);
            System.out.println(SERVICE_JAR_DST_DIR_STRING + " directory successfully created!");
            try (final InputStream SERVICE_JAR_SRC = Installer.class.getResourceAsStream(SERVICE_JAR_SRC_STRING)) {
                if (SERVICE_JAR_SRC == null) {
                    System.out.println("Resource not found: " + SERVICE_JAR_SRC_STRING);
                    return false;
                }
                System.out.println("Path String: " + SERVICE_JAR_DST_DIR_PATH);
                Files.copy(SERVICE_JAR_SRC, SERVICE_JAR_DST_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
                return true;
            }

            // catch (AccessDeniedException ade) {
            // final String errorMessageString = "An error occurred while moving uptime
            // logger service to "
            // + SERVICE_JAR_DST_DIR_PATH;
            // System.out.println(errorMessageString);
            // throw new RuntimeException(errorMessageString);
            // }
        } catch (AccessDeniedException ade) {
            final String errorMessageString = "Your user does not have permission create directories in /opt. Please run the app using a user that has permission to read and write in /opt such as sudo";
            System.out.println(errorMessageString);
            throw new RuntimeException(errorMessageString);
        } catch (IOException e1) {
            // e1.printStackTrace();
            System.out.println("Could not create directory " + SERVICE_JAR_DST_DIR_STRING);
        }
        return false;
    }

    public static boolean isServiceInstalled() {
        return Files.exists(SERVICE_JAR_DST_FILE_PATH) && Files.exists(SERVICE_INSTALL_DST_FILE_PATH);
    }

    public static boolean isServiceEnabled() {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "systemctl is-enabled " + Config.SERVICE_INSTALL_NAME);
            final Process process = processBuilder.start();
            try ( final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                final String line = bufferedReader.readLine(); // Read first line of output
                process.waitFor(); 
                return line.equals("enabled");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
    // public static boolean isServiceEnabled() {
    //     try {
    //         final ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c",
    //                 "systemctl list-units --type=service --all | grep " + Config.SERVICE_INSTALL_NAME);
    //         final Process process = processBuilder.start();
    //         try (final BufferedReader bufferedReader = new BufferedReader(
    //                 new InputStreamReader(process.getInputStream()))) {
    //             String line;
    //             while ((line = bufferedReader.readLine()) != null) {
    //                 if (line.contains(Config.SERVICE_NAME)) {
    //                     return true;
    //                 }
    //             }
    //         }
    //         process.waitFor();
    //     } catch (IOException | InterruptedException e) {
    //         e.printStackTrace();
    //     }
    //     return false;
    // }

    private static boolean isSudoUser() {
        final ProcessBuilder processBuilder = new ProcessBuilder("id", "-u");
        try {
            final Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                final String uid = reader.readLine();
                return "0".equals(uid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean executeCommand(String command) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);
        try {
            final Process process = processBuilder.start();
            final int exitCode = process.waitFor();
            if (exitCode == 0) {
                return true;
            }
        } catch (IOException | InterruptedException e) {
            // e.printStackTrace();
        }
        return false;
    } 
    public static void startService() {
        sudoUserCheck();
        final boolean serviceIsNotEnabled=!isServiceEnabled();
        if (serviceIsNotEnabled) {
            final String title = "Start Service Issue";
            final String header = "Service is not enabled";
            final String message = "There was a problem while starting the service. Service is not enabled";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
        final boolean commandFailed = !executeCommand(String.format("sudo systemctl start %s", serviceName));
        if (commandFailed) {
            final String title = "Start Service Issue";
            final String header = "There was a problem while starting the service";
            final String message = "Failed start the service";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        } 
    }
    public static void stopService() {
        sudoUserCheck();
        final boolean serviceIsNotEnabled=!isServiceEnabled();
        if (serviceIsNotEnabled) {
            final String title = "Stop Service Issue";
            final String header = "Service is not enabled";
            final String message = "There was a problem while stopping the service. Service is not enabled";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
        final boolean commandFailed = !executeCommand(String.format("sudo systemctl stop %s", serviceName));
        if (commandFailed) {
            final String title = "Stop Service Issue";
            final String header = "There was a problem while stopping the service";
            final String message = "Failed stop the service";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
    }

    public static void disableService() {
        sudoUserCheck();
        final boolean serviceIsNotInstalled=!isServiceInstalled();
        if (serviceIsNotInstalled) {
            final String title = "Disable Service Issue";
            final String header = "Service is not installed";
            final String message = "There was a problem while disabling the service. Service is not installed";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
        final boolean commandFailed = !executeCommand(String.format("sudo systemctl disable %s", serviceName));
        if (commandFailed) {
            final String title = "Disable Service Issue";
            final String header = "There was a problem while disabling the service";
            final String message = "Failed disable the service";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
    }
    public static void enableService() {
        sudoUserCheck();
        final boolean serviceIsNotInstalled=!isServiceInstalled();
        if (serviceIsNotInstalled) {
            final String title = "Enable Service Issue";
            final String header = "Service is not installed";
            final String message = "There was a problem while enabling the service. Please install service first";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
        final boolean commandFailed = !executeCommand(String.format("sudo systemctl enable %s", serviceName));
        if (commandFailed) {
            final String title = "Enable Service Issue";
            final String header = "There was a problem while enabling the service";
            final String message = "Failed enable the service";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
    }

    public static void removeServiceConfigFile() {
        try {
            Files.deleteIfExists(SERVICE_INSTALL_DST_FILE_PATH);
        } catch (IOException e) {
            final String title = "Removing Service Config Issue";
            final String header = "There was a problem while removing the service config file";
            final String message = "Failed to remove the service config file";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
        // final boolean commandFailed=!executeCommand(String.format("sudo rm %s",
        // SERVICE_INSTALL_DST_FILE_PATH));
        // if (commandFailed) {
        // final String title = "Removing Service Config Issue";
        // final String header = "There was a problem while removing the service config
        // file";
        // final String message = "Failed to remove the service config file";
        // final String errorMessage = String.format("%s,%s,%s", title, header,
        // message);
        // throw new RuntimeException(errorMessage);
        // }
    }

    public static void removeServiceJarFile() {
        // TODO add location of jar,save file and service file location in help/about
        // menu
        try {
            Files.deleteIfExists(SERVICE_JAR_DST_FILE_PATH);
        } catch (IOException e) {
            final String title = "Removing Service Executable Issue";
            final String header = "There was a problem while removing the service executable file";
            final String message = "Failed to remove the service executable file";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
    }

    public static void uninstallService() {
        sudoUserCheck();
        // Stop the service
        stopService();
        // Disable the service
        disableService();
        // Remove the service config file
        removeServiceConfigFile();
        // Remove the service jar file
        removeServiceJarFile();
        // Reload systemd
        executeCommand("sudo systemctl daemon-reload");

    }

    private static void sudoUserCheck() {
        final boolean isNotSudoUser = !isSudoUser();
        if (isNotSudoUser) {
            final String title = "Authorization Issue";
            final String header = "Your user is not authorized to uninstall this service";
            final String message = "Please run the app using a user that has permission uninstall services(sudo)";
            final String errorMessage = String.format("%s,%s,%s", title, header, message);
            throw new RuntimeException(errorMessage);
        }
    }

}
