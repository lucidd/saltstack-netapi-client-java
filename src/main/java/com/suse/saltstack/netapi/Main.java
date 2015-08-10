package com.suse.saltstack.netapi;

import com.suse.saltstack.netapi.calls.modules.Test;
import com.suse.saltstack.netapi.calls.runner.Manage;
import com.suse.saltstack.netapi.calls.wheel.Key;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.target.Glob;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;

public class Main {
    
    public static void main(String[] args) throws SaltStackException {
        SaltStackClient authorized = new SaltStackClient(URI.create("http://hoag.suse.de:9080"));
        authorized.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        authorized.login("admin", "", AuthModule.AUTO);
        SaltStackClient unauthorized = new SaltStackClient(URI.create("http://hoag.suse.de:9080"));
        unauthorized.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        
        authorized.callSync(Test.ping(), new Glob());
        unauthorized.callSync(Test.ping(), new Glob(), "admin", "", AuthModule.AUTO);

        authorized.callSync(Key.finger("*"));
        unauthorized.callSync(Key.finger("*"), "admin", "", AuthModule.AUTO);

        authorized.callSync(Manage.present());
        unauthorized.callSync(Manage.present(), "admin", "", AuthModule.AUTO);
    }
    
}
