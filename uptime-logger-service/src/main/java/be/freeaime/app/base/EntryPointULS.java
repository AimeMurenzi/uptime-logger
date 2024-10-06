package be.freeaime.app.base;

import java.io.IOException;


public class EntryPointULS {
    public static final String ruler = "════════════════════════════════════════════════════════════════════════";
    // TODO: add command to list records boot date and with their uptime
    public static void main(String... args) throws IOException {
        final String osName = System.getProperty("os.name").toLowerCase();
        final boolean serviceIsNotRunningOnLinux = !osName.contains("linux");
        if (serviceIsNotRunningOnLinux) {
            System.out.println("This uptime logging service only runs on Linux distributions currently. Exiting");
            return;
        }
        if (args.length > 0) {
            handleArgs(args);
            return;
        }
        printAppVersion();
        RecordManager.updateRecord();
        RecordManager.printUptimeRecord();
        final long fiveMinutes = 300000;
        while (true) {
            try {
                Thread.sleep(fiveMinutes);
                RecordManager.updateRecord();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleArgs(String... args) {
        switch (args[0].toLowerCase()) {
            case "-u":
                RecordManager.printUptimeRecord();
                return;
            case "--uptime":
                RecordManager.printUptimeRecord();
                return;
                case "-a":
                RecordManager.printUptimeRecord();
                return;
            case "--all":
                RecordManager.printUptimeRecord();
            case "-v":
                printAppVersion();
                return;
            case "--version":
                printAppVersion();
                return;
            default:
                System.out.println("-v --version Displays application version");
                System.out.println("-u --uptime Show uptime");
                System.out.println("-a --all Show all recorded uptime");
                break;
        }
    }

    private static void printAppVersion() {
        final String version = PropertyUtil.getProperty("app.parent.version");
        final String title = PropertyUtil.getProperty("app.name");
        if (title != null && version != null) {
            System.out.println(ruler);
            System.out.println(title.toUpperCase());
            System.out.println("VERSION " + version);
            System.out.println(ruler);
        }
    }



}
