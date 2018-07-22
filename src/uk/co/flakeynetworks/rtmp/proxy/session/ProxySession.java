package uk.co.flakeynetworks.rtmp.proxy.session;

import uk.co.flakeynetworks.rtmp.proxy.protocol.ProxyProtocol;

import java.util.UUID;

public class ProxySession {

    public static final int STATE_CONNECTION_OPENED = 0;
    public static final int STATE_APPLICATION_CONFIRMED = 1;
    public static final int STATE_PROTOCOL_CONFIRMED = 2;
    public static final int STATE_RTMP_DESTINATION_SET = 3;
    public static final int STATE_RTMP_DESTINATION_CONNECTED = 4;

    protected int state = STATE_CONNECTION_OPENED;

    private String sessionUID;
    private ProxyProtocol protocol;

    public ProxySession() {

        // Create a Universally unique ID that this session can be referenced.
        UUID uuid = UUID.randomUUID();
        sessionUID = uuid.toString();
    } // end of constructor


    public ProxySession(String UUID) {

        this.sessionUID = UUID;
    } // end of constructor


    public String getSessionUID() { return sessionUID; } // end of getSessionUID


    public int getState() { return state; } // end of getState


    public void setProtocol(ProxyProtocol protocol) {

        this.protocol = protocol;
    } // end of setProtocol
} // end of ProxySession
