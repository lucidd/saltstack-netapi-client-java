package com.suse.saltstack.netapi.calls.modules;

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.util.Map;
import java.util.Optional;

/**
 * salt.modules.test
 */
public class Test {

    private static final LocalCall<Map<String, Boolean>> PING =
            new LocalCall<>("test.ping", Optional.empty(), Optional.empty(),
            new TypeToken<Map<String, Boolean>>(){});

    public static LocalCall<Map<String, Boolean>> ping() {
        return PING;
    }
}
