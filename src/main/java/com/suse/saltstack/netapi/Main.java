package com.suse.saltstack.netapi;

import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.Token;

import java.net.URI;

public class Main {

    public static void main(String[] args) throws Throwable {
        String hoag = "hoag.suse.de:9080";
        String sumars = "sumars-head.suse.de:9080";
        String local  = "172.17.0.10:8000";
        SaltStackClient client = new SaltStackClient(URI.create("http://" + local));
        client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        Token token = client.login("salt", "", AuthModule.AUTO);
        System.out.println(token.getToken());
    }

}
