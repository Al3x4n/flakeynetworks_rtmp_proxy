package uk.co.flakeynetworks.rtmp.proxy.protocol;

import uk.co.flakeynetworks.rtmp.proxy.ProxyClient;
import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionClient;

import java.nio.ByteBuffer;

public class ProxyProtocolClient extends ProxyProtocol {


    private ProxySessionClient session;


    public ProxyProtocolClient(ProxySessionClient session) {

        super(session);

        this.session = session;
        session.setProtocol(this);
    } // end of constructor


    public void sendApplicationHandshake() {

        // Write the application handshake
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(ProxyProtocol.HEADER_APPLICATION_HANDSHAKE).append(ProxyProtocol.APPLICATION_HANDSHAKE);

        sendMessageToEndpoint(messageBuilder.toString().getBytes());

        System.out.println("Proxy Client Message Counter: " + messageCounter +  " sent application handshake");
    } // end of sendApplicationHandshake


    public void sendProtocolHandshake() {

        // Send the protocol version
        byte[] message = new byte[5];
        message[0] = (byte) HEADER_PROTOCOL_HANDSHAKE;

        System.arraycopy(ByteBuffer.allocate(4).putInt(ProxyClient.PROTOCOL_VERSION).array(), 0, message,1,4);

        // Write out the protocol handshake
        sendMessageToEndpoint(message);

        System.out.println("Proxy Client Message Counter: " + messageCounter +  " sent protocol handshake");
    } // end of sendHandshake


    public void sendConnectToDestination() {

        byte[] message = new byte[5];
        message[0] = (byte) HEADER_RTMP_DESTINATION_CONNECT;

        // Write out the protocol handshake
        sendMessageToEndpoint(message);

        System.out.println("Proxy Client Message Counter: " + messageCounter +  " sent rtmp connect command");
    } // sendConnectToDestination


    public void sendRTMPDestination(String rtmpDestinationKey) {

        int keyLength = rtmpDestinationKey.length();

        int messageLength = 1 + 4 + keyLength;

        byte[] message = new byte[messageLength];
        message[0] = HEADER_RTMP_DESTINATION;


        byte[] lengthArray = ByteBuffer.allocate(4).putInt(keyLength).array();
        System.arraycopy(lengthArray, 0, message,1,4);

        byte[] contents = rtmpDestinationKey.getBytes();
        System.arraycopy(contents, 0, message,5,keyLength);

        // Write out the protocol handshake
        sendMessageToEndpoint(message);

        System.out.println("Proxy Client Message Counter: " + messageCounter + " sent rtmp destination");
    } // end of sendRTMPDestination


    @Override
    public void processRequest(int bytes_read, byte[] contents) {

        if(contents == null || bytes_read == 0) return;

        // Get the header
        char header = (char) contents[0];
        switch(header) {

            case ProxyProtocol.HEADER_APPLICATION_HANDSHAKE:

                // TODO check if this handshake is out of position. It should be the very first message sent.

                session.applicationHandshakeCompleted();
                break;

            case ProxyProtocol.HEADER_PROTOCOL_HANDSHAKE:

                // TODO check if this handshake is out of position. It should be the very first message sent.

                session.applicationProtocolCompleted();
                break;

            case ProxyProtocol.HEADER_RTMP_DESTINATION:

                if(bytes_read < 2) {
                    System.err.println("Error! Proxy Client incorrect length for rtmp destination confirmation field. Expecting: 1 bytes for confirmation field, found: " + (bytes_read - 1) + " bytes, expecting: 1 byte");
                    return;
                } // end of if

                boolean set = contents[1] == (byte) 0x01;
                session.setRTMPDestinationConfirmed(set);
                break;

            case ProxyProtocol.HEADER_RTMP_DESTINATION_CONNECT:

                if(bytes_read < 2) {
                    System.err.println("Error! Proxy Client incorrect length for rtmp destination connect confirmation field. Expecting: 1 bytes for confirmation field, found: " + (bytes_read - 1) + " bytes, expecting: 1 byte");
                    return;
                } // end of if

                boolean success = contents[1] == (byte) 0x01;
                session.setRTMPDestinationConnectConfirmed(success);
                break;

            case HEADER_DATA:

                if(bytes_read < 5) {
                    System.err.println("Error! Proxy Client incorrect length for data. Expecting: 4 bytes for message length field, found: " + (bytes_read - 1) + " bytes");
                    return;
                } // end of if

                byte[] lengthField = new byte[4];
                for(int i = 0; i < 4; i++)
                    lengthField[i] = contents[i+1];

                int dataLength = toInt(lengthField);

                if(bytes_read < dataLength + 5) {
                    System.err.println("Error! Proxy Client incorrect length for data. Expecting: " + dataLength + " bytes for data field, found: " + (bytes_read - 5) + " bytes");
                    return;
                } // end of if

                byte[] data = new byte[dataLength];

                System.arraycopy(contents, 5, data, 0, dataLength);

                session.sendDataToSource(data);

                break;
        } // end of switch
    } // end of processBytes
} // end of ProxyProtocolClient
