package uk.co.flakeynetworks.rtmp.proxy.channel;

import uk.co.flakeynetworks.rtmp.proxy.session.ProxySession;
import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

public class ProxyChannel {


    private ProxySession session;
    private final Socket socket;
    private final InputStream inFromEndpoint;
    private final OutputStream outToEndpoint;
    private ProxyProtocol protocol;

    private long byteCounter = 0;
    private Thread incomingData = new ThreadIncomingData();


    class ThreadIncomingData extends Thread {

        @Override
        public void run() {

            final byte[] request = new byte[1024];

            int bytes_read;
            try {
                while ((bytes_read = inFromEndpoint.read(request)) != -1) {

                    byteCounter += bytes_read;

                    protocol.processRequest(bytes_read, request);
                } // end of while
            } catch (IOException e) {

                e.printStackTrace();
            } // end of catch
        } // end of run
    } // end of ThreadIncomingData



    public ProxyChannel(Socket socket) throws IOException {

        this.socket = socket;

        inFromEndpoint = socket.getInputStream();
        outToEndpoint = socket.getOutputStream();

        // Start processing data
        incomingData.start();
    } // end of constructor


    public SocketAddress getEndpointIP() {

        return socket.getRemoteSocketAddress();
    } // end of getEndpointIP


    public ProxyProtocol getProtocol() { return protocol; } // end of getProtocol


    public void setProtocol(ProxyProtocol proxyProtocol) {

        this.protocol = proxyProtocol;
    } // end of setProtocol


    public void send(byte[] message) throws IOException {

        outToEndpoint.write(message);
    } // end of send
} // end of ProxyChannel
