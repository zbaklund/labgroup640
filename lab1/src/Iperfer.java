import java.net.*;
import java.io.*;
import org.apache.commons.cli.*; // Apache command line parsing library

// Implements Iperfer command line utility
public class Iperfer {
    private static int portNum = -1;
    private static final int MILLISECONDS = 1000;

    public static void main(String[] args) throws Exception {
        // Lazily parse the command line args
        Options options = new Options();

        // Add command arg options
        Option client = new Option("c", "client", false, "Set client mode");
        options.addOption(client);
        Option hostname = new Option("h", "hostname", true, "Server hostname");
        options.addOption(hostname);
        Option time = new Option("t", "time", true, "Time");
        options.addOption(time);
        Option server = new Option("s", "server", false, "Set server mode");
        options.addOption(server);
        Option port = new Option("p", "port", true, "Port");
        port.setRequired(true);
        options.addOption(port);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        }

        // Get port
        if(cmd.getOptionValue("p") == null) {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        } else {
            portNum = Integer.parseInt(cmd.getOptionValue("p"));
            if(portNum > 65535 || portNum < 1024) {
                System.out.println("Error: port number must be in the range 1024 to 65535");
                System.exit(1);
            }
        }

        try {
            // Client Mode
            if (cmd.hasOption("c")) {
                // Get time
                double timeVal = Integer.parseInt(cmd.getOptionValue("t"));
                timeVal *= MILLISECONDS;

                String host = cmd.getOptionValue("h");
                Socket c = null;
                OutputStream outStream = null;

                // Open socket
                try {
                    c = new Socket(host, portNum);
                    outStream = c.getOutputStream();
                } catch (IOException e) {
                    System.out.println("Error: invalid arguments");
                    System.exit(1);
                }
                long start = System.currentTimeMillis();
                int numArraysSent = 0;

                byte dataToSend[] = new byte[1000];

                // Send byte streams
                while (System.currentTimeMillis() - start < timeVal ) {
                    try {
                        outStream.write(dataToSend); // Keep trying to write a null byte array
                    } catch (Exception e) {
                        System.out.println("Failed to send byte array");
                        System.exit(1);
                    }
                    numArraysSent++;
                }
                c.close();
                outStream.close();

                // Print statistics
                System.out.println("sent=" + numArraysSent + "KB rate=" + String.format("%.3f", (numArraysSent / (timeVal / MILLISECONDS)) / 1000) + " Mbps");
            }

            // Server mode
            else if (cmd.hasOption("s")) {
                Socket s = null;
                ServerSocket serverSocket = null;
                int socketReturnBytes = 0;

                // Open socket
                try {
                    serverSocket = new ServerSocket(portNum);
                    s = serverSocket.accept();
                } catch (Exception e) {
                    System.out.println("An error occurred when trying to create a server socket");
                    System.exit(1);
                }
                // Get start time
                long start = System.currentTimeMillis();
                int numBytesReceived = 0;
                // Receive data until there is no more
                byte[] buffer = new byte[1000];
                while(socketReturnBytes > -1) {
                    socketReturnBytes = s.getInputStream().read(buffer);
                    numBytesReceived += socketReturnBytes;
                }
                long end = System.currentTimeMillis();
                s.close();
                serverSocket.close();

                double timeReceiving = end - start;
                // Calculate and print stats
                System.out.println("received=" + numBytesReceived / 1000 + "KB rate="
                        + String.format("%.3f", ((numBytesReceived / 1000) / ((timeReceiving) / MILLISECONDS)) / 1000) + " Mbps");
                System.exit(0); // Exit gracefully
            }
            // Bad args
            else {
                System.out.println("Error: invalid arguments");
                System.exit(1);
            }
            // Lazy catchall for non-passed arguments / wrong args
        } catch (NumberFormatException e) {
            System.out.println("Error: invalid arguments");
            System.exit(1);
        }
    }
}
