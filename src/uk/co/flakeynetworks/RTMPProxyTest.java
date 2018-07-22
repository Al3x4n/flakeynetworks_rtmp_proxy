package uk.co.flakeynetworks;

import uk.co.flakeynetworks.rtmp.proxy.ProxyClient;
import uk.co.flakeynetworks.rtmp.proxy.ProxyServer;
import uk.co.flakeynetworks.rtmp.RTMPDestination;
import uk.co.flakeynetworks.vmix.VmixAPI;

import java.io.IOException;

public class RTMPProxyTest {


    static Thread vmixStartStreaming = new Thread(() -> {

        try {

            Thread.sleep(500);

            VmixAPI api = new VmixAPI();
            api.startStreaming();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } // end of catch
    }); // end of thread


    public static void vMixStartStreaming() {

        // Start the vmix streaming
        vmixStartStreaming.start();

        System.out.println("Proxy Client Started vMix Streaming");
    } // end of vMixStartStreaming


    public static ProxyClient setupClient() throws IOException {

        // Setup the ProxyClient
        ProxyClient proxyClient = new ProxyClient("127.0.0.1", 1936);
        proxyClient.setCredentials("username", "password");

        // Set number of connections channels to be used
        proxyClient.setNumberOfChannels(1);

        // Connect to the server
        boolean success = proxyClient.connect();

        return proxyClient;
    } // end of setupClient


    public static void main(String[] args) {

        try {

            // Setup the ProxyServer
            ProxyServer proxyServer = new ProxyServer();
            proxyServer.start();


            // Setup a rtmpDestination and add it to the server.
            String youtubeRTMPHost = "a.rtmp.youtube.com";
            int youtubeRTMPPort = 1935;
            RTMPDestination rtmpDestination = new RTMPDestination(youtubeRTMPHost, youtubeRTMPPort);

            proxyServer.addDestination(rtmpDestination);


            // Setup the ProxyClient
            ProxyClient proxyClient = setupClient();
            proxyClient.setRTMPDestination(rtmpDestination.getDestinationUUID());


            // Connect to the rtmp destination
            //rtmpDestination.connect();
        } catch (Exception e) {

            System.err.println("Something went wrong somewhere");
            e.printStackTrace();
        } // end of catch
    } // end of main
} // end of RTMPProxyTest
