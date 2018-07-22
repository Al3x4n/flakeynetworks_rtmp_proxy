package uk.co.flakeynetworks.rtmp.proxy.protocol;

import uk.co.flakeynetworks.rtmp.proxy.session.ProxySession;
import uk.co.flakeynetworks.rtmp.proxy.channel.ProxyChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ProxyProtocol {

    /** Header to handshake for a protocol use. **/
    public final static char HEADER_PROTOCOL_HANDSHAKE = '1';
    public final static char HEADER_APPLICATION_HANDSHAKE = '0';
    public final static char HEADER_RTMP_DESTINATION = '2';
    public final static char HEADER_RTMP_DESTINATION_CONNECT = '3';
    public final static char HEADER_DATA = '4';


    protected long messageCounter = 0;


    /** Reserved for future use to allow for more protocol headers **/
    public final static char HEADER_PROTOCOL_EXTEND = 'Z';

    /**
     * Random String to send to upon connection to ensure the correct application is connecting.
     */
    public final static String APPLICATION_HANDSHAKE = "32bn339!2";


    private ProxySession session;

    protected int nextChannelIndexToUse = 0;


    protected final List<ProxyChannel> channels = new ArrayList<>();


    public ProxyProtocol(ProxySession session) {

        this.session = session;
        session.setProtocol(this);
    } // end of constructor


    public void processRequest(int bytes_read, byte[] contents) {  } // end of processBytes


    public void addChannel(ProxyChannel channel) {

        channels.add(channel);
        channel.setProtocol(this);
    } // end of addChannel


    public void sendMessageToEndpoint(byte[] message) {

        // Use round robin technique
        // TODO add in weighted option
        ProxyChannel channel = channels.get(nextChannelIndexToUse++);

        // Check if we need to go back to the first channel to use next.
        if(nextChannelIndexToUse >= channels.size())
            nextChannelIndexToUse = 0;

        try {
            channel.send(message);

            messageCounter++;
        } catch (IOException e) {
            e.printStackTrace();
        } // end of catch
    } // end of sendMessageToEndpoint


    public static int toInt( byte[] bytes ) {

        int l = 0;
        l |= bytes[0] & 0xFF;
        l <<= 8;
        l |= bytes[1] & 0xFF;
        l <<= 8;
        l |= bytes[2] & 0xFF;
        l <<= 8;
        l |= bytes[3] & 0xFF;

        return l;
    } // end of toInt


    public void sendData(int bytes_read, byte[] reply) {

        int remaining = bytes_read;
        int indexCounter = 0;

        while(remaining > 0) {

            int size = remaining;
            if (size > 1019) size = 1019;

            // Send the data
            byte[] message = new byte[size + 5];

            // Add the header
            message[0] = HEADER_DATA;

            // Add the length
            byte[] lengthArray = ByteBuffer.allocate(4).putInt(size).array();
            System.arraycopy(lengthArray, 0, message, 1, 4);

            // Copy the contents
            System.arraycopy(reply, indexCounter, message, 5, size);

            // Write out the data
            sendMessageToEndpoint(message);

            indexCounter += size;
            remaining -= size;
        } // end of while
    } // end of sendData
} // end of ProxyProtocol
