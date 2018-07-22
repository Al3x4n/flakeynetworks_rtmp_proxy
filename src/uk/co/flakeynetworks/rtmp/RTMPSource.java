package uk.co.flakeynetworks.rtmp;

import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RTMPSource {


    private int port;
    private ServerSocket socketListener;
    private ListenerThread listenerThread;
    private ProcessingThread processingThread;
    private ProxySessionClient session;
    private Socket socket;
    private InputStream inFromClient;
    private OutputStream outToClient;


    public RTMPSource(ProxySessionClient session, int port) {

        this.port = port;
        this.session = session;
    } // end of constructor


    public void startProcessing() {

        if(processingThread != null) return;

        processingThread = new ProcessingThread();
        processingThread.start();
    } // end of startProcessing


    public void sendData(byte[] data) {

        try {
            outToClient.write(data);
        } catch (IOException e) {

            e.printStackTrace();
            // TODO have to handle this exception
        } // end of catch
    } // end of sendData


    class ProcessingThread extends Thread {

        @Override
        public void run() {

            try {
                inFromClient = socket.getInputStream();
                outToClient = socket.getOutputStream();

                // current thread manages streams from server to client (download)
                int bytes_read;
                byte[] reply = new byte[4096];

                while ((bytes_read = inFromClient.read(reply)) != -1)
                    session.sendDataToDestination(bytes_read, reply);
            } catch (IOException e) {

                System.out.println("Connection Closed from RTMP source.");
            } finally {
                try {
                    outToClient.close();
                    inFromClient.close();
                    socket.close();
                } catch (IOException ignored) { } // end of catch
            } // end of finally
        } // end of run
    } // end of ProcessingThread


    class ListenerThread extends Thread {

        @Override
        public void run() {

            try {
                socketListener = new ServerSocket(port);

                System.out.println("Proxy Client Started to listen for RTMP connections");

                while (true) {

                    try {
                        Socket accepted = socketListener.accept();

                        if(socket != null) {

                            accepted.close();
                            continue;
                        } // end of of


                        socket = accepted;

                        session.rtmpSourceConnected(RTMPSource.this);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } // end of catch
                } // end of while
            } catch (IOException e) {
                e.printStackTrace();
            } // end of catch
        } // end of run
    } // end of ListenerThread


    public void close() {

        try {
            if (socket != null)
                socket.close();
        } catch (IOException ignored) { } // end of catch
    } // end of close


    public void start() {

        // Check to see if there is already listening thread running.
        if(listenerThread != null) return;

        // Create a new thread to run and listen.
        listenerThread = new ListenerThread();
        listenerThread.start();
    } // end of start
} // end of RTMPListener
