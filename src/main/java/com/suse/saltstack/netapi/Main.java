package com.suse.saltstack.netapi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.Token;
import com.suse.saltstack.netapi.event.EventListener;
import com.suse.saltstack.netapi.event.EventStream;
import com.suse.saltstack.netapi.event.WebSocketObservable;
import com.suse.saltstack.netapi.parser.JsonParser;
import com.suse.saltstack.netapi.utils.ClientUtils;
import rx.Observable;

import java.net.URI;

public class Main {


    public static void old() throws Throwable {
        SaltStackClient client = new SaltStackClient(URI.create("http://hoag.suse.de:8765"));
        client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        Token token = client.login("admin", "", AuthModule.AUTO);
        System.out.println(token.getToken());
        EventStream stream = client.events();
        stream.addEventListener(new EventListener() {
            @Override
            public void notify(String event) {
                System.out.println("Event:" + event);
            }

            @Override
            public void eventStreamClosed() {
                System.out.println("closed");
            }
        });
    }

    public static void streams() throws Throwable {
        SaltStackClient client = new SaltStackClient(URI.create("http://hoag.suse.de:8765"));
        client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        Token token = client.login("admin", "", AuthModule.AUTO);
        System.out.println(token.getToken());
        Observable<String> stream = WebSocketObservable.websocket(URI.create("ws://hoag.suse.de:8765/ws/" + token.getToken()));
        Gson gson = new GsonBuilder().create();
        stream.map(String::trim)
              .filter(s -> !s.equals("server received message"))
              .map(s -> s.substring(6))
              .map(ClientUtils::stringToStream)
              .map(JsonParser.EVENT::parse)
              .map(e -> e.getTag())
              .forEach(System.out::println);
    }

    public static void main(String[] args) throws Throwable {
        streams();
        Thread.sleep(10000000);
    }

}
