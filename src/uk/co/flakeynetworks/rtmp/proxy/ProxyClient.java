package uk.co.flakeynetworks.rtmp.proxy;


import uk.co.flakeynetworks.RTMPProxyTest;
import uk.co.flakeynetworks.rtmp.RTMPSource;
import uk.co.flakeynetworks.rtmp.proxy.channel.ProxyChannelClient;
import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocolClient;
import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionClient;

import java.io.IOException;
import java.net.Socket;

public class ProxyClient {

    // INT is 32 bits (4 bytes)
    public static int PROTOCOL_VERSION = 1;
    public static int DEFAULT_LISTENING_PORT = 1936;

    public static int DEFAULT_NUMBER_CHANNELS = 1;

    public String rtmpDestinationKey;

    private String serverHost;
    private int serverPort = ProxyServer.DEFAULT_PORT;
    private int numOfChannels = DEFAULT_NUMBER_CHANNELS;
    private String username;
    private String password;


    private int listeningPort = DEFAULT_LISTENING_PORT;
    private RTMPSource rtmpSource;


    private ProxySessionClient session;


    public ProxyClient(String serverAddress, int listeningPort) {

        super();

        serverHost = serverAddress;
        this.listeningPort = listeningPort;
    } // end of ProxyClient


    public void setNumberOfChannels(int channelNumber) {

        // Do some validation. Can never have lesser than one channel to the server.
        numOfChannels = channelNumber < 1 ? 1 : channelNumber;
    } // end of setNumberOfChannels


    public boolean connect() throws IOException {

        // Setup one connection first
        Socket connectionToServer = new Socket(serverHost, serverPort);

        System.out.println("Proxy Client connected to: " + serverHost + ":" + serverPort);

        session = new ProxySessionClient(this);
        ProxyProtocolClient protocol = new ProxyProtocolClient(session);

        ProxyChannelClient channel = new ProxyChannelClient(connectionToServer);
        protocol.addChannel(channel);

        // Initialise the connection
        protocol.sendApplicationHandshake();

        return true;

        // TODO once connected do we need to add in a handshake which includes a version in it. Maybe get a list of supported protocols?
        // TODO do we also include header information such as commands for x y z to make it future proof if we need to add in more commands
        // TODO authentication server should send over a hash the password and username should be hashed with to be sent over the wire. username should also be hashed with a pre-defined key.
    } // end of connect


    public void setCredentials(String username, String password) {

        this.username = username;
        this.password = password;
    } // end of setCredentials


    public void setRTMPDestination(String destinationUUID) {

        rtmpDestinationKey = destinationUUID;
    } // end of setRTMPDestination


    public String getRTMPDestination() { return rtmpDestinationKey; } // end of getRTMPDestination


    public void startListening() {

        rtmpSource = new RTMPSource(session, listeningPort);
        rtmpSource.start();

        // TODO REMOVE THIS FOR PRODUCTION. THIS IS FOR TESTING
        RTMPProxyTest.vMixStartStreaming();
    } // end of startListening
} // end of ProxyClient
