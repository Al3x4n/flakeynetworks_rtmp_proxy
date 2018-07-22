package uk.co.flakeynetworks.vmix;

import uk.co.flakeynetworks.web.ParameterStringBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class VmixAPI {


    public static final String DEFAULT_VMIX_ADDRESS = "127.0.0.1";
    public static final int DEFAULT_VMIX_PORT = 8088;
    public static final String DEFAULT_VMIX_PROTOCOL = "http://";

    private String vmixAddress = DEFAULT_VMIX_ADDRESS;
    private URL vmixUrl;
    private int vmixPort = DEFAULT_VMIX_PORT;


    public VmixAPI(String address, int port) throws MalformedURLException {

        this.vmixAddress = address;
        this.vmixPort = port;

        vmixUrl = new URL(DEFAULT_VMIX_PROTOCOL + vmixAddress + ":" + vmixPort + "/api/");
    } // end of constructor


    public VmixAPI() throws MalformedURLException {

        vmixUrl = new URL(DEFAULT_VMIX_PROTOCOL + vmixAddress + ":" + vmixPort + "/api/");
    } // end of default constructor


    public boolean startStreaming() throws IOException {



        Map<String, String> parameters = new HashMap<>();
        parameters.put("Function", "StartStreaming");


        HttpURLConnection con = (HttpURLConnection) vmixUrl.openConnection();
        con.setRequestMethod("GET");

        con.setDoOutput(true);

        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(ParameterStringBuilder.getParamsString(parameters));
        out.flush();
        out.close();


        int status = con.getResponseCode();
        if(status == 200) return true;

        return false;
    } // end of startStreaming
} // end of VmixAPI
