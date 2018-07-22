package uk.co.flakeynetworks.rtmp.proxy;

import uk.co.flakeynetworks.rtmp.RTMPDestination;
import uk.co.flakeynetworks.rtmp.proxy.channel.ProxyChannel;
import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocolServer;
import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionServer;
import uk.co.flakeynetworks.rtmp.proxy.session.ProxySession;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyServer {

    private final static int[] SUPPORTED_PROTOCOLS = {1};

    public static final int DEFAULT_PORT = 30000;

    private int port = DEFAULT_PORT;
    private ServerSocket socketListener;
    private ListenerThread listenerThread;


    private final List<RTMPDestination> destinationList = new ArrayList<>();
    private final Map<String, RTMPDestination> destinationMap = new HashMap<>();

    private final List<ProxySession> sessionsList = new ArrayList<>();


    public void addDestination(RTMPDestination rtmpDestination) {

        if(rtmpDestination == null) return;

        destinationList.add(rtmpDestination);
        destinationMap.put(rtmpDestination.getDestinationUUID(), rtmpDestination);
    } // end of addDestination


    public RTMPDestination getRTMPDestination(String key) {

        return destinationMap.get(key);
    } // end of getRTMPDestination


    class ListenerThread extends Thread {

        @Override
        public void run() {

            while (true) {
                try {

                    ProxySessionServer session = new ProxySessionServer(ProxyServer.this);
                    sessionsList.add(session);

                    ProxyProtocolServer protocol = new ProxyProtocolServer(session);

                    ProxyChannel channel = new ProxyChannel(socketListener.accept());
                    protocol.addChannel(channel);


                    System.out.println("Proxy Server Connection accepted from: " + channel.getEndpointIP());
                } catch (IOException e) {
                    e.printStackTrace();
                } // end of catch
            } // end of while
        } // end of run
    } // end of ListenerThread


    public ProxyServer(int port) {

        this.port = port;
    } // end of constructor


    public ProxyServer() {
    } // end of constructor


    public void start() throws IOException {

        // Check to see if there is already listening thread running.
        if(socketListener != null || listenerThread != null) return;

        // Setup the listener
        socketListener = new ServerSocket(port);

        // Create a new thread to run and listen.
        listenerThread = new ListenerThread();
        listenerThread.start();

        System.out.println("Proxy Server started on port: " + port);
    } // end of start


    public static boolean isProtocolSupported(int protocolVersion) {

        for(int protocol : SUPPORTED_PROTOCOLS) {

            if(protocolVersion == protocol)
                return true;
        } // end of for

        return false;
    } // end of isProtocolSupported
} // end of ProxyServer
