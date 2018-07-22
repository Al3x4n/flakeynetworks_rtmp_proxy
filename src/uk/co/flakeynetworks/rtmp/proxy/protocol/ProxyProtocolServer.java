package uk.co.flakeynetworks.rtmp.proxy.protocol;

import uk.co.flakeynetworks.rtmp.proxy.ProxyServer;
import uk.co.flakeynetworks.rtmp.proxy.session.ProxySessionServer;


public class ProxyProtocolServer extends ProxyProtocol {

    private ProxySessionServer session;

    public ProxyProtocolServer(ProxySessionServer session) {

        super(session);

        this.session = session;
        session.setProtocol(this);
    } // end of constructor


    public void sendApplicationAck() {

        // Write the application handshake
        byte[] bytes = {ProxyProtocol.HEADER_APPLICATION_HANDSHAKE};
        sendMessageToEndpoint(bytes);
    } // end of sendApplicationHandshake



    public void sendProtocolAck() {

        // Write the application handshake
        byte[] bytes = {ProxyProtocol.HEADER_PROTOCOL_HANDSHAKE};
        sendMessageToEndpoint(bytes);
    } // end of sendProtocolAck


    @Override
    public void processRequest(int bytes_read, byte[] contents) {

        if(contents == null || bytes_read == 0) return;

        // Get the header
        char header = (char) contents[0];
        byte[] lengthField;
        switch(header) {

            // Todo should move these into functions

            case ProxyProtocol.HEADER_APPLICATION_HANDSHAKE:

                // TODO check if this handshake is out of position. It should be the very first message sent.

                int expectedBytes = ProxyProtocol.APPLICATION_HANDSHAKE.length();
                if(bytes_read < expectedBytes + 1) {
                    System.err.println("Error! Proxy Server incorrect length for application handshake. Expecting: " + expectedBytes + " bytes, found: " + (bytes_read - 1) + " bytes");
                    return;
                } // end of if

                char[] message = new char[expectedBytes];
                for(int i = 0; i < expectedBytes; i++)
                    message[i] = (char) contents[i+1];

                String messageString = String.valueOf(message);
                if(ProxyProtocol.APPLICATION_HANDSHAKE.equals(messageString))
                    session.applicationHandshakeCompleted();

                break;

            case ProxyProtocol.HEADER_PROTOCOL_HANDSHAKE:

                if(bytes_read < 5) {
                    System.err.println("Error! Proxy Server incorrect length for protocol handshake. Expecting: 4 bytes, found: " + (bytes_read - 1) + " bytes\"");
                    return;
                } // end of if

                byte[] protocolMessage = new byte[4];
                for(int i = 0; i < 4; i++)
                    protocolMessage[i] = contents[i+1];

                int protocolVersion = toInt(protocolMessage);
                if(ProxyServer.isProtocolSupported(protocolVersion)) {
                    session.protocolHandshakeCompleted();
                } else {

                    System.err.println("Error! Proxy Server protocol by client is not supported. Requested Protocol: " + protocolVersion);
                    // TODO close down the session here.
                } // end of else

                break;

            case ProxyProtocol.HEADER_RTMP_DESTINATION:

                if(bytes_read < 5) {
                    System.err.println("Error! Proxy Server incorrect length for setting rtmp destination. Expecting: 4 bytes for message length field, found: " + (bytes_read - 1) + " bytes");
                    return;
                } // end of if


                lengthField = new byte[4];
                for(int i = 0; i < 4; i++)
                    lengthField[i] = contents[i+1];

                int length = toInt(lengthField);

                if(bytes_read < length + 5) {
                    System.err.println("Error! Proxy Server incorrect length for setting rtmp key. Found: " + (bytes_read - 5) + " bytes, expecting: " + length + " bytes");
                    return;
                } // end of if

                char[] rtmpKey = new char[length];
                for(int i = 0; i < length; i++)
                    rtmpKey[i] = (char) contents[i + 5];

                String key = String.valueOf(rtmpKey);

                session.setRTMPDestination(key);

                break;

            case HEADER_RTMP_DESTINATION_CONNECT:

                session.rtmpDestinationConnect();
                break;

            case HEADER_DATA:

                if(bytes_read < 5) {
                    System.err.println("Error! Proxy Server incorrect length for data. Expecting: 4 bytes for message length field, found: " + (bytes_read - 1) + " bytes");
                    return;
                } // end of if

                lengthField = new byte[4];
                for(int i = 0; i < 4; i++)
                    lengthField[i] = contents[i+1];

                int dataLength = toInt(lengthField);

                if(bytes_read < dataLength + 5) {
                    System.err.println("Error! Proxy Server incorrect length for data. Expecting: " + dataLength + " bytes for data field, found: " + (bytes_read - 5) + " bytes");
                    return;
                } // end of if

                byte[] data = new byte[dataLength];

                System.arraycopy(contents, 5, data, 0, dataLength);

                session.sendDataToDestination(data);

                // TODO DO WE NEED TO SEND BACK A CHECKSUM
                break;
        } // end of switch
    } // end of processBytes


    public void sendDestinationAck(boolean b) {

        // Write the application handshake
        byte[] bytes = { ProxyProtocol.HEADER_RTMP_DESTINATION, b ? (byte) 0x01 : (byte) 0x00};

        sendMessageToEndpoint(bytes);
    } // end of sendDestinationAck


    public void sendRTMPDestinationConnectAck(boolean success) {

        // Write the application handshake
        byte[] bytes = { ProxyProtocol.HEADER_RTMP_DESTINATION_CONNECT, success ? (byte) 0x01 : (byte) 0x00};

        sendMessageToEndpoint(bytes);
    } // end of sendRTMPDestinationConnectAck
} // end of ProxyProtocolServer
