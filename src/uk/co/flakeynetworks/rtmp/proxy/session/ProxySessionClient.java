package uk.co.flakeynetworks.rtmp.proxy.session;

import uk.co.flakeynetworks.rtmp.RTMPSource;
import uk.co.flakeynetworks.rtmp.proxy.ProxyClient;
import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocolClient;

public class ProxySessionClient extends ProxySession {


    private ProxyProtocolClient protocol;
    private ProxyClient client;
    private RTMPSource source;


    public ProxySessionClient(ProxyClient client) {

        this.client = client;
    } // end of constructor


    public void setProtocol(ProxyProtocolClient protocol) {

        this.protocol = protocol;
    } // end of setProtocol


    public void applicationHandshakeCompleted() {

        System.out.println("Proxy Client application handshake completed");

        state = STATE_APPLICATION_CONFIRMED;

        protocol.sendProtocolHandshake();
    } /// end of applicationHandshakeCompleted


    public void applicationProtocolCompleted() {

        System.out.println("Proxy Client protocol handshake completed");

        state = STATE_PROTOCOL_CONFIRMED;

        // Setup the RTMP destination to send to
        setRTMPDestination(client.getRTMPDestination());
    } // end of applicationProtocolCompleted


    public void setRTMPDestination(String rtmpDestinationKey) {

        protocol.sendRTMPDestination(rtmpDestinationKey);
    } // end of setRTMPDestination


    public void setRTMPDestinationConfirmed(boolean truth) {

        System.out.println("Proxy Client rtmp destination set: " + truth);

        if(truth) {
            state = STATE_RTMP_DESTINATION_SET;

            client.startListening();
        } // end of if
    } // end of setRTMPDestination


    public void rtmpSourceConnected(RTMPSource source) {

        System.out.println("Proxy Client Connection accepted from RTMP Source");

        this.source = source;

        // Message the server to connect to the rtmp destination
        protocol.sendConnectToDestination();
    }  // end of rtmpSourceConnected


    public void setRTMPDestinationConnectConfirmed(boolean success) {

        System.out.println("Proxy Client Connection to RTMP destination confirmed: " + success);

        if(success) {
            state = STATE_RTMP_DESTINATION_CONNECTED;
            source.startProcessing();
        } else {
            source.close();
            source = null;
        } // end of else
    } // end of setRTMPDestinationConnectConfirmed


    public void sendDataToDestination(int bytes_read, byte[] reply) {

        protocol.sendData(bytes_read, reply);
    } // end of sendDataToDestination


    public void sendDataToSource(byte[] data) {

        source.sendData(data);
    } // end of sendDataToSource
} // end of ProxySessionClient
