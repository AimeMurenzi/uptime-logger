package be.freeaime.app.uptimeloggermanager;
import java.util.*;
import java.io.*;


import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
public class Tool {
    private class Holder {
    private static Tool INSTANCE=new Tool();
       
    }
    public Tool getInstance(){
        return Holder.INSTANCE;
    }
    
    private Tool(){}
    public static List<Long> serviceRunningCheck() {
        final List<Long> uptimeLoggerPidList = new ArrayList<>();
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (VirtualMachineDescriptor vmDescriptor : vms) {
            System.out.println(vmDescriptor.displayName());
            System.out.println(Manager.ruler);
            if (vmDescriptor.displayName().contains(Config.SERVICE_NAME)) {
                final long pid = Long.parseLong(vmDescriptor.id());
                uptimeLoggerPidList.add(pid);
            }
        }
        return uptimeLoggerPidList;
    }
    public static List<Long> stopAllUptimeLoggers() {
        final List<Long> uptimeLoggerPidList=serviceRunningCheck();
        final List<Long> failedClosures = new ArrayList<>();
        uptimeLoggerPidList.forEach(uptimeLoggerPid -> {
            try {
                final ProcessHandle handle = ProcessHandle.of(uptimeLoggerPid).orElseThrow(
                        () -> new RuntimeException("Failed to get process handle for PID: " + uptimeLoggerPid));
                final boolean failedToTernateProcess = !handle.destroyForcibly();
                if (failedToTernateProcess)
                    failedClosures.add(uptimeLoggerPid);
            } catch (Exception e) {
                failedClosures.add(uptimeLoggerPid);
            }
        });
        return failedClosures;
    }
    public static boolean isServiceEnabled() {
        try {
            final ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "systemctl is-enabled " + Config.SERVICE_NAME);
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
    public static void saveToTextFile(String dataString, String pathToFile) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToFile))) {
            bufferedWriter.write(dataString);
        }
    }

}
