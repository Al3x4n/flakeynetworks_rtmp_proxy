package uk.co.flakeynetworks.rtmp;

import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionServer;

import java.io.*;
import java.net.Socket;
import java.util.UUID;

public class RTMPDestination {

    private String destinationUUID;

    private final String destinationHost;
    private final int destinationPort;
    private Socket serverSocket;
    private InputStream inFromServer;
    private OutputStream outToServer;
    private ProcessingThread processingThread;
    private ProxySessionServer session;


    class ProcessingThread extends Thread {

        @Override
        public void run() {

            try {

                // current thread manages streams from server to client (download)
                int bytes_read;
                byte[] reply = new byte[4096];

                while ((bytes_read = inFromServer.read(reply)) != -1) {

                    session.sendDataToSource(bytes_read, reply);
                } // end of while
            } catch (IOException e) {

                System.out.println("Connection Closed from RTMP destination.");
            } finally {
                try {
                    inFromServer.close();
                    outToServer.close();
                    serverSocket.close();

                    serverSocket = null;
                    inFromServer = null;
                    outToServer = null;
                } catch (IOException ignored) { } // end of catch
            } // end of finally
        } // end of run
    } // end of ProcessingThread


    public RTMPDestination(String destinationHost, int destinationPort) {

        this.destinationHost = destinationHost;
        this.destinationPort = destinationPort;

        // Create a Universally unique ID that this session can be referenced.
        UUID uuid = UUID.randomUUID();
        destinationUUID = uuid.toString();
    } // end of RTMPDestination


    public void setSession(ProxySessionServer proxySessionServer) {

        this.session = proxySessionServer;
    } // end of setSession


    public boolean connect() {

        // Connect to the server.
        try {

            serverSocket = new Socket(destinationHost, destinationPort);

            inFromServer = serverSocket.getInputStream();
            outToServer = serverSocket.getOutputStream();

            processingThread = new ProcessingThread();
            processingThread.start();
        } catch (IOException e) {

            return false;
        } // end of catch

        return true;
    } // end of connect


    public String getDestinationUUID() { return destinationUUID; } // end of getDestinationUUID


    public void send(byte[] data) {

        try {

            outToServer.write(data);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            // TODO need to handle this somehow
        } // end of catch
    } // end of send
} // end of RTMPDestination
