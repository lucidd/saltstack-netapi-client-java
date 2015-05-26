package com.suse.saltstack.netapi.client;


import com.suse.saltstack.netapi.config.*;
import org.apache.http.*;
import org.apache.http.auth.*;
import org.apache.http.client.*;
import org.apache.http.client.config.*;
import org.apache.http.impl.client.*;

import java.util.Optional;

enum HttpMethod {
    GET,
    POST
}

interface HttpConnection {
    void request(HttpMethod method, Optional<String> data);
}

public interface ConnectionBuilder {

    void setConnectTimeout(int timeout);

    void setSocketTimeout(int timeout);

    void setProxySettings(ProxySettings proxySettings);

    HttpConnection build();

}

class HttpBuilder implements ConnectionBuilder {

    HttpClientBuilder httpClientBuilder;
    RequestConfig.Builder requestConfigBuilder;

    public HttpBuilder() {
        httpClientBuilder = HttpClients.custom();
    }

    @Override
    public void setConnectTimeout(int timeout) {
        requestConfigBuilder.setConnectTimeout(timeout);
    }

    @Override
    public void setProxySettings(ProxySettings proxySettings) {
        httpClientBuilder.setProxy(new HttpHost(proxySettings.getHostname(), proxySettings.getPort()));
        proxySettings.getCredentials().ifPresent((credentials) -> {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(proxySettings.getHostname(), proxySettings.getPort()),
                    new UsernamePasswordCredentials(credentials.getUsername(), credentials.getPassword()));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        });
    }

    @Override
    public void setSocketTimeout(int timeout) {
        requestConfigBuilder.setSocketTimeout(timeout);
    }

    @Override
    public HttpConnection build() {
        return null;
    }
}
