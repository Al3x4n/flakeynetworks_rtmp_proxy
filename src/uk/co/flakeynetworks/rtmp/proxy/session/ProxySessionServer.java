package uk.co.flakeynetworks.rtmp.proxy.session;

import uk.co.flakeynetworks.rtmp.RTMPDestination;
import uk.co.flakeynetworks.rtmp.proxy.ProxyServer;
import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocolServer;

public class ProxySessionServer extends ProxySession {


    private ProxyProtocolServer protocol;
    private ProxyServer server;
    private RTMPDestination destination;


    public ProxySessionServer(ProxyServer server) {

        this.server = server;
    } // end of constructor


    public void setProtocol(ProxyProtocolServer protocol) {

        this.protocol = protocol;
    } // end of setProtocol


    public void applicationHandshakeCompleted() {

        System.out.println("Proxy Server application handshake successfully found.");

        state = STATE_APPLICATION_CONFIRMED;

        protocol.sendApplicationAck();
    } // end of applicationHandshakeCompleted


    public void protocolHandshakeCompleted() {

        System.out.println("Proxy Server protocol handshake successfully found.");

        state = STATE_PROTOCOL_CONFIRMED;

        protocol.sendProtocolAck();
    } //  end of protocolHandshakeCompleted


    public void setRTMPDestination(String key) {

        destination = server.getRTMPDestination(key);
        destination.setSession(this);

        protocol.sendDestinationAck(destination != null);
    } // end of setRTMPDestination


    public void rtmpDestinationConnect() {

        boolean success = destination.connect();

        System.out.println("Proxy Server Connection to RTMP destination: " + success);

        protocol.sendRTMPDestinationConnectAck(success);
    } // end of set


    public void sendDataToDestination(byte[] data) {

        destination.send(data);
    } // end of sendDataToDestination


    public void sendDataToSource(int bytes_read, byte[] reply) {

        protocol.sendData(bytes_read, reply);
    } // end of sendDataToSource
} // end of ProxySessionServer
