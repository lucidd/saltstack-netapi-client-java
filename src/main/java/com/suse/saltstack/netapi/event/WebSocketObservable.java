package com.suse.saltstack.netapi.event;

import rx.*;

import javax.websocket.*;
import java.io.*;
import java.net.*;

public class WebSocketObservable {

    @ClientEndpoint
    public static class Websocket {

        final URI uri;
        final Subscriber<? super String> subscriber;
        Session session;

        public Websocket(URI uri, Subscriber<? super String> subscriber) {
            this.uri = uri;
            this.subscriber = subscriber;
        }

        @OnOpen
        public void onOpen(Session session, EndpointConfig config) throws IOException {
            session.getBasicRemote().sendText("websocket client ready");
            subscriber.onStart();
        }

        @OnMessage
        public void onMessage(String event) {
            if(!subscriber.isUnsubscribed()) {
                subscriber.onNext(event);
            }
        }

        @OnError
        public void onError(Throwable t) {
            subscriber.onError(t);
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            //TODO: check close reason and call onComplete or onError accordingly
            subscriber.onCompleted();
        }
    }

    static class OnSubscribeWebsocket implements Observable.OnSubscribe<String> {

        final WebSocketContainer websocketContainer = ContainerProvider.getWebSocketContainer();
        final URI uri;

        public OnSubscribeWebsocket(URI uri) {
            this.uri = uri;
        }

        @Override
        public void call(Subscriber<? super String> subscriber) {
            try {
                Websocket ws = new Websocket(uri, subscriber);
                Session session = websocketContainer.connectToServer(ws, uri);
                session.setMaxIdleTimeout(0);
            } catch (Throwable e) {
                subscriber.onError(e);
            }
        }
    }

    public static Observable<String> websocket(URI uri) {
        return Observable.create(new OnSubscribeWebsocket(uri));
    }


}
