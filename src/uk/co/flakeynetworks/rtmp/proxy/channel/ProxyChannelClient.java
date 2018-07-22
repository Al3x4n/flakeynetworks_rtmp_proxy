package uk.co.flakeynetworks.rtmp.proxy.channel;

import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocolClient;

import java.io.IOException;
import java.net.Socket;

public class ProxyChannelClient extends ProxyChannel {


    protected ProxyProtocolClient protocol;


    public ProxyChannelClient(Socket socket) throws IOException {

        super(socket);

        // Easier to overload the protocol as ProxyProtocolClient so that we don't have to continuously cast
        this.protocol = protocol;
    } // end of constructor


    public ProxyProtocolClient getProtocol() { return protocol; } // end of getProtocol
} // end of ProxyChannelClient
