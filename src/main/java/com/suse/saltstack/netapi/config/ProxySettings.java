package com.suse.saltstack.netapi.config;

import com.suse.saltstack.netapi.client.SaltStackClient;

import java.util.*;

/**
 * Class representing proxy settings to be used with {@link SaltStackClient}.
 */
public class ProxySettings {
    public static class Credentials {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    private String hostname;
    private int port;
    private Optional<Credentials> credentials;

    /**
     * Basic constructor taking proxy hostname and port.
     *
     * @param hostname proxy hostname
     * @param port proxy port
     */
    public ProxySettings(String hostname, int port) {
        this(hostname, port, Optional.empty());
    }

    /**
     * Extended constructor supporting proxies with authentication.
     *
     * @param hostname proxy hostname
     * @param port proxy port
     * @param credentials proxy credentials
     */
    public ProxySettings(String hostname, int port, Optional<Credentials> credentials) {
        this.hostname = hostname;
        this.port = port;
        this.credentials = credentials;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return the proxy port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the proxy credentials
     */
    public Optional<Credentials> getCredentials() {
        return credentials;
    }
}
