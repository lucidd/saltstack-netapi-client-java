import com.suse.saltstack.netapi.*;
import com.suse.saltstack.netapi.client.*;
import com.suse.saltstack.netapi.datatypes.*;
import com.suse.saltstack.netapi.event.*;
import com.suse.saltstack.netapi.event.EventListener;

import java.net.*;
import java.util.stream.*;

public class Main {

    public static void main(String[] args) throws Exception {

        WebSocketObservable.websocket(null);

        SaltStackClient client = new SaltStackClient(URI.create("http://172.17.0.31:8000"));
        Token token = client.login("salt", "", AuthModule.AUTO);
        System.out.println(token.getToken());
        client.events().addEventListener(new EventListener() {

            @Override
            public void notify(String event) {
                System.out.println("Event: " + event);
            }

            @Override
            public void eventStreamClosed() {
                System.out.println("stream closed");
            }

        });
    }

}
