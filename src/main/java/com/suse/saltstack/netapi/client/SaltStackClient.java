package com.suse.saltstack.netapi.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.calls.*;
import com.suse.saltstack.netapi.client.impl.HttpClientConnectionFactory;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.config.ProxySettings;
import com.suse.saltstack.netapi.datatypes.Job;
import com.suse.saltstack.netapi.datatypes.Keys;
import com.suse.saltstack.netapi.datatypes.ScheduledJob;
import com.suse.saltstack.netapi.datatypes.Token;
import com.suse.saltstack.netapi.datatypes.cherrypy.Stats;
import com.suse.saltstack.netapi.datatypes.target.Target;
import com.suse.saltstack.netapi.event.EventStream;
import com.suse.saltstack.netapi.exception.SaltStackException;
import com.suse.saltstack.netapi.parser.JsonParser;
import com.suse.saltstack.netapi.results.Result;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * SaltStack API client.
 */
public class SaltStackClient {

    /** The configuration object */
    private final ClientConfig config = new ClientConfig();

    /** The connection factory object */
    private final ConnectionFactory connectionFactory;

    /** The executor for async operations */
    private final ExecutorService executor;

    private final Gson gson = new GsonBuilder().create();

    /**
     * Constructor for connecting to a given URL.
     *
     * @param url the SaltStack URL
     */
    public SaltStackClient(URI url) {
        this(url, new HttpClientConnectionFactory());
    }

    /**
     * Constructor for connecting to a given URL using a specific connection factory.
     *
     * @param url the SaltStack URL
     * @param connectionFactory ConnectionFactory implementation
     */
    public SaltStackClient(URI url, ConnectionFactory connectionFactory) {
        this(url, connectionFactory, Executors.newCachedThreadPool());
    }

    /**
     * Constructor for connecting to a given URL.
     *
     * @param url the SaltStack URL
     * @param executor ExecutorService to be used for async operations
     */
    public SaltStackClient(URI url, ExecutorService executor) {
        this(url, new HttpClientConnectionFactory(), executor);
    }

    /**
     * Constructor for connecting to a given URL using a specific connection factory.
     *
     * @param url the SaltStack URL
     * @param connectionFactory ConnectionFactory implementation
     * @param executor ExecutorService to be used for async operations
     */
    public SaltStackClient(URI url, ConnectionFactory connectionFactory,
            ExecutorService executor) {
        // Put the URL in the config
        config.put(ClientConfig.URL, url);
        this.connectionFactory = connectionFactory;
        this.executor = executor;
    }

    /**
     * Directly access the configuration.
     *
     * @return the configuration object
     */
    public ClientConfig getConfig() {
        return config;
    }

    /**
     * Configure to use a proxy when connecting to the SaltStack API.
     *
     * @param settings the proxy settings
     */
    public void setProxy(ProxySettings settings) {
        if (settings.getHostname() != null) {
            config.put(ClientConfig.PROXY_HOSTNAME, settings.getHostname());
            config.put(ClientConfig.PROXY_PORT, settings.getPort());
        }
        if (settings.getUsername() != null) {
            config.put(ClientConfig.PROXY_USERNAME, settings.getUsername());
            if (settings.getPassword() != null) {
                config.put(ClientConfig.PROXY_PASSWORD, settings.getPassword());
            }
        }
    }

    /**
     * Perform login and return the token.
     * <p>
     * {@code POST /login}
     *
     * @param username the username
     * @param password the password
     * @param eauth the eauth type
     * @return the authentication token
     * @throws SaltStackException if anything goes wrong
     */
    public Token login(final String username, final String password, final AuthModule eauth)
            throws SaltStackException {
        Map<String, String> props = new LinkedHashMap<>();
        props.put("username", username);
        props.put("password", password);
        props.put("eauth", eauth.getValue());

        String payload = gson.toJson(props);

        Result<List<Token>> result = connectionFactory
                .create("/login", JsonParser.TOKEN, config)
                .getResult(payload);

        // For whatever reason they return a list of tokens here, take the first
        Token token = result.getResult().get(0);
        config.put(ClientConfig.TOKEN, token.getToken());
        return token;
    }

    /**
     * Asynchronously perform login and return a Future with the token.
     * <p>
     * {@code POST /login}
     *
     * @param username the username
     * @param password the password
     * @param eauth the eauth type
     * @return Future containing the authentication token
     */
    public Future<Token> loginAsync(final String username, final String password,
            final AuthModule eauth) {
        return executor.submit(() -> login(username, password, eauth));
    }

    /**
     * Perform logout and clear the session token from the config.
     * <p>
     * {@code POST /logout}
     *
     * @return true if the logout was successful, otherwise false
     * @throws SaltStackException if anything goes wrong
     */
    public boolean logout() throws SaltStackException {
        Result<String> stringResult = connectionFactory
                .create("/logout", JsonParser.STRING, config).getResult("");
        String logoutMessage = "Your token has been cleared";
        boolean result = logoutMessage.equals((stringResult.getResult()));
        if (result) {
            config.remove(ClientConfig.TOKEN);
        }
        return result;
    }

    /**
     * Asynchronously perform logout and clear the session token from the config.
     * <p>
     * {@code POST /logout}
     *
     * @return Future containing a boolean result, true if logout was successful
     */
    public Future<Boolean> logoutAsync() {
        return executor.submit(this::logout);
    }

    /**
     * Query for all minions and immediately return a map of minions keyed by minion id.
     * <p>
     * {@code GET /minions}
     *
     * @return map containing maps representing minions, keyed by minion id
     * @throws SaltStackException if anything goes wrong
     * @see <a href="http://docs.saltstack.com/en/latest/topics/targeting/grains.html">
     *     Grains</a>
     */
    public Map<String, Map<String, Object>> getMinions() throws SaltStackException {
        return connectionFactory.create("/minions", JsonParser.RETMAPS, config)
                .getResult().getResult().get(0);
    }

    /**
     * Asynchronously query for all minions and return a map of minions keyed by minion id.
     * <p>
     * {@code GET /minions}
     *
     * @return Future with a map containing maps representing minions, keyed by minion id
     * @throws SaltStackException if anything goes wrong
     * @see <a href="http://docs.saltstack.com/en/latest/topics/targeting/grains.html">
     *     Grains</a>
     */
    public Future<Map<String, Map<String, Object>>> getMinionsAsync()
            throws SaltStackException {
        return executor.submit(this::getMinions);
    }

    /**
     * Query for details (grains) of the specified minion.
     * <p>
     * {@code GET /minions/<minion-id>}
     *
     * @return Map key: grain name, value: grain value
     * @throws SaltStackException if anything goes wrong
     * @see <a href="http://docs.saltstack.com/en/latest/topics/targeting/grains.html">
     *     Grains</a>
     */
    public Map<String, Object> getMinionDetails(String minionId) throws SaltStackException {
        return connectionFactory.create("/minions/" + minionId, JsonParser.RETMAPS, config)
                .getResult().getResult().get(0).get(minionId);
    }

    /**
     * Query for details (grains) of the specified minion asynchronously.
     * <p>
     * {@code GET /minions/<minion-id>}
     *
     * @return Future with a map containing details of the minion
     * @throws SaltStackException if anything goes wrong
     * @see <a href="http://docs.saltstack.com/en/latest/topics/targeting/grains.html">
     *     Grains</a>
     */
    public Future<Map<String, Object>> getMinionDetailsAsync(final String minionId)
            throws SaltStackException {
        return executor.submit(() -> getMinionDetails(minionId));
    }

    /**
     * Generic interface to start any execution command and immediately return an object
     * representing the scheduled job.
     * <p>
     * {@code POST /minions}
     *
     * @param target the target
     * @param function the function to execute
     * @param args list of non-keyword arguments
     * @param kwargs map containing keyword arguments
     * @return object representing the scheduled job
     * @throws SaltStackException if anything goes wrong
     */
    public <T> ScheduledJob startCommand(final Target<T> target, final String function,
            List<Object> args, Map<String, Object> kwargs) throws SaltStackException {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("tgt", target.getTarget());
        props.put("expr_form", target.getType());
        props.put("fun", function);
        props.put("arg", args);
        props.put("kwarg", kwargs);

        String payload = gson.toJson(Collections.singleton(props));

        // Connect to the minions endpoint and send the above lowstate data
        Result<List<ScheduledJob>> result = connectionFactory
                .create("/minions", JsonParser.SCHEDULED_JOB,  config)
                .getResult(payload);

        // They return a list of tokens here, we take the first
        return result.getResult().get(0);
    }

    /**
     * Asynchronously start any execution command and immediately return an object
     * representing the scheduled job.
     * <p>
     * {@code POST /minions}
     *
     * @param target the target
     * @param function the function to execute
     * @param args list of non-keyword arguments
     * @param kwargs map containing keyword arguments
     * @return Future containing the scheduled job
     */
    public <T> Future<ScheduledJob> startCommandAsync(final Target<T> target,
            final String function, final List<Object> args,
            final Map<String, Object> kwargs) {
        return executor.submit(() -> startCommand(target, function, args, kwargs));
    }

    /**
     * Query for the result of a supplied job.
     * <p>
     * {@code GET /job/<job-id>}
     *
     * @param job {@link ScheduledJob} object representing scheduled job
     * @return Map key: minion id, value: command result from that minion
     * @throws SaltStackException if anything goes wrong
     */
    public Map<String, Object> getJobResult(final ScheduledJob job)
            throws SaltStackException {
        return getJobResult(job.getJid());
    }

    /**
     * Query for the result of a supplied job.
     * <p>
     * {@code GET /job/<job-id>}
     *
     * @param job String representing scheduled job
     * @return Map key: minion id, value: command result from that minion
     * @throws SaltStackException if anything goes wrong
     */
    public Map<String, Object> getJobResult(final String job) throws SaltStackException {
        Result<List<Map<String, Object>>> result = connectionFactory
                .create("/jobs/" + job, JsonParser.RETVALS, config)
                .getResult();

        // A list with one element is returned, we take the first
        return result.getResult().get(0);
    }

    /**
     * Get previously run jobs.
     * <p>
     * {@code GET /jobs}
     *
     * @return map containing run jobs keyed by job id
     * @throws SaltStackException if anything goes wrong
     */
    public Map<String, Job> getJobs() throws SaltStackException {
        Result<List<Map<String, Job>>> result = connectionFactory
                .create("/jobs", JsonParser.JOBS, config)
                .getResult();
        return result.getResult().get(0);
    }

    /**
     * Asynchronously get previously run jobs.
     * <p>
     * {@code GET /jobs}
     *
     * @return Future with a map containing run jobs keyed by job id
     */
    public Future<Map<String, Job>> getJobsAsync() {
        return executor.submit(this::getJobs);
    }

    /**
     * Generic interface to start any execution command bypassing normal session handling.
     * <p>
     * {@code POST /run}
     *
     * @param username the username
     * @param password the password
     * @param eauth the eauth type
     * @param client the client
     * @param target the target
     * @param function the function to execute
     * @param args list of non-keyword arguments
     * @param kwargs map containing keyword arguments
     * @return Map key: minion id, value: command result from that minion
     * @throws SaltStackException if anything goes wrong
     */
    public <T> Map<String, Object> run(final String username, final String password,
            final AuthModule eauth, final String client, final Target<T> target,
            final String function, List<Object> args, Map<String, Object> kwargs)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.put("username", username);
        props.put("password", password);
        props.put("eauth", eauth.getValue());
        props.put("client", client);
        props.put("tgt", target.getTarget());
        props.put("expr_form", target.getType());
        props.put("fun", function);
        props.put("arg", args);
        props.put("kwarg", kwargs);

        List<Map<String, Object>> list =  Collections.singletonList(props);

        String payload = gson.toJson(list);
        System.out.println("Payload: " + payload);

        Result<List<Map<String, Object>>> result = connectionFactory
                .create("/run", JsonParser.RETVALS, config)
                .getResult(payload);

        // A list with one element is returned, we take the first
        return result.getResult().get(0);
    }

    
    public <R> R callSync(final LocalCall<R> call, Target<?> target)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.put("tgt", target.getTarget());
        props.put("expr_form", target.getType());
        Result<List<JsonElement>> wrapper = call(call, Client.LOCAL, "/", props, new TypeToken<Result<List<JsonElement>>>(){});
        return gson.fromJson(wrapper.getResult().get(0), call.getReturnType().getType());
    }

    public <R> R callSync(final WheelCall<R> call) throws SaltStackException {
        Result<List<WheelResult<JsonElement>>> wrapper = call(call, Client.WHEEL, "/", null, new TypeToken<Result<List<WheelResult<JsonElement>>>>(){});
        return gson.fromJson(wrapper.getResult().get(0).getData().getResult(), call.getReturnType().getType());
    }

    public <R> R callSync(final RunnerCall<R> call)
            throws SaltStackException {
        Result<List<JsonElement>> wrapper = call(call, Client.RUNNER, "/", null, new TypeToken<Result<List<JsonElement>>>(){});
        return gson.fromJson(wrapper.getResult().get(0), call.getReturnType().getType());
    }
    
    
    
    public <R> R callSync(final LocalCall<R> call, Target<?> target, String username, String password, AuthModule authModule)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.putAll(call.payload());
        props.put("username", username);
        props.put("password", password);
        props.put("eauth", authModule.getValue());
        props.put("tgt", target.getTarget());
        props.put("expr_form", target.getType());
        Result<List<JsonElement>> wrapper = call(call, Client.LOCAL, "/run", props,
                new TypeToken<Result<List<JsonElement>>>() {});
        JsonElement inner = wrapper.getResult().get(0);
        return gson.fromJson(inner, call.getReturnType().getType());
    }

    private <R> R call(Call<?> call, Client client, String endpoint, Map<String, Object> custom, TypeToken<R> type)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.putAll(call.payload());
        props.put("client", client.getValue());
        if(custom != null) {
            props.putAll(custom);
        }

        List<Map<String, Object>> list =  Collections.singletonList(props);

        String payload = gson.toJson(list);

        System.out.println("Payload: " + payload);

        return connectionFactory
                .create(endpoint, new JsonParser<>(type), config)
                .getResult(payload);
    }

    public <R> R callSync(WheelCall<R> call, String username, String password, AuthModule authModule)
            throws SaltStackException {
        Result<List<WheelResult<JsonElement>>> wrapper = callSync(call, Client.WHEEL, username, password, authModule,
                new TypeToken<Result<List<WheelResult<JsonElement>>>>() {
                });
        JsonElement inner = wrapper.getResult().get(0).getData().getResult();
        return gson.fromJson(inner, call.getReturnType().getType());
    }

    public <R> R callSync(final RunnerCall<R> call, String username, String password, AuthModule authModule)
            throws SaltStackException {
        Result<List<JsonElement>> wrapper = callSync(call, Client.RUNNER, username, password, authModule,
                new TypeToken<Result<List<JsonElement>>>(){});
        JsonElement inner = wrapper.getResult().get(0);
        return gson.fromJson(inner, call.getReturnType().getType());
    }


    

    public <R> LocalAsyncResult<R> callAsync(final LocalCall<R> call, Target<?> target)
            throws SaltStackException {

        Map<String, Object> props = new HashMap<>();
        props.putAll(call.payload());
        props.put("client", Client.LOCAL_ASYNC.getValue());
        props.put("tgt", target.getTarget());
        props.put("expr_form", target.getType());

        List<Map<String, Object>> list =  Collections.singletonList(props);

        String payload = gson.toJson(list);

        System.out.println("Payload: " + payload);

        Result<List<JsonElement>> result = connectionFactory
                .create("/", new JsonParser<>(new TypeToken<Result<List<JsonElement>>>(){}), config)
                .getResult(payload);

        return gson.fromJson(result.getResult().get(0), new TypeToken<LocalAsyncResult<R>>() {
        }.getType());
    }

    public <R> WheelAsyncResult<R> callAsync(final RunnerCall<R> call)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.putAll(call.payload());
        props.put("client", Client.RUNNER_ASYNC.getValue());

        List<Map<String, Object>> list =  Collections.singletonList(props);

        String payload = gson.toJson(list);

        System.out.println("Payload: " + payload);

        Result<List<JsonElement>> wrapper = connectionFactory
                .create("/", new JsonParser<>(new TypeToken<Result<List<JsonElement>>>(){}), config)
                .getResult(payload);

        return gson.fromJson(wrapper.getResult().get(0), new TypeToken<WheelAsyncResult>() {
        }.getType());
    }

    public <R> WheelAsyncResult<R> callAsync(final WheelCall<R> call)
            throws SaltStackException {
        Map<String, Object> props = new HashMap<>();
        props.putAll(call.payload());
        props.put("client", Client.WHEEL_ASYNC.getValue());

        List<Map<String, Object>> list =  Collections.singletonList(props);

        String payload = gson.toJson(list);

        System.out.println("Payload: " + payload);

        Result<List<JsonElement>> wrapper = connectionFactory
                .create("/", new JsonParser<>(new TypeToken<Result<List<JsonElement>>>(){}), config)
                .getResult(payload);

        return gson.fromJson(wrapper.getResult().get(0), new TypeToken<WheelAsyncResult>() {
        }.getType());
    }


    /**
     * Asynchronously start any execution command bypassing normal session handling.
     * <p>
     * {@code POST /run}
     *
     * @param username the username
     * @param password the password
     * @param eauth the eauth type
     * @param client the client
     * @param target the target
     * @param function the function to execute
     * @param args list of non-keyword arguments
     * @param kwargs map containing keyword arguments
     * @return Future containing Map key: minion id, value: command result from that minion
     */
    public <T> Future<Map<String, Object>> runAsync(final String username,
            final String password, final AuthModule eauth, final String client,
            final Target<T> target, final String function, final List<Object> args,
            final Map<String, Object> kwargs) {
        return executor.submit(() ->
                run(username, password, eauth, client, target, function, args, kwargs));
    }

    /**
     * Query statistics from the CherryPy Server.
     * <p>
     * {@code GET /stats}
     *
     * @return the stats
     * @throws SaltStackException if anything goes wrong
     */
    public Stats stats() throws SaltStackException {
        return connectionFactory.create("/stats", JsonParser.STATS, config).getResult();
    }

    /**
     * Asynchronously query statistics from the CherryPy Server.
     * <p>
     * {@code GET /stats}
     *
     * @return Future containing the stats
     */
    public Future<Stats> statsAsync() {
        return executor.submit(this::stats);
    }

    /**
     * Query general key information.
     * <p>
     * Required permissions: {@code @wheel}
     * <p>
     * {@code GET /keys}
     *
     * @return the keys
     * @throws SaltStackException if anything goes wrong
     */
    public Keys keys() throws SaltStackException {
        return connectionFactory.create("/keys", JsonParser.KEYS, config).getResult()
                .getResult();
    }

    /**
     * Asynchronously query general key information.
     * <p>
     * Required permissions: {@code @wheel}
     * <p>
     * {@code GET /keys}
     *
     * @return Future containing the keys
     */
    public Future<Keys> keysAsync() {
        return executor.submit(this::keys);
    }

    /**
     * Returns a WebSocket @ClientEndpoint annotated object connected
     * to the /ws ServerEndpoint.
     * <p>
     * The stream object supports the {@link EventStream} interface which allows the caller
     * to register/unregister for stream event notifications as well as close the event
     * stream.
     * <p>
     * Note: {@link SaltStackClient#login(String, String, AuthModule)} or
     * {@link SaltStackClient#loginAsync(String, String, AuthModule)} must be called prior
     * to calling this method.
     * <p>
     * {@code GET /events}
     *
     * @return the event stream
     */
    public EventStream events() {
        return new EventStream(config);
    }

    /**
     * Trigger an event in Salt with the specified tag and data.
     * <p>
     * {@code POST /hook}
     *
     * @param eventTag the event tag
     * @param eventData the event data. Must be valid JSON.
     * @return the boolean value returned by Salt. If true the event was triggered
     * successfully.
     * A value of false is returned only if Salt itself returns false; it does not mean a
     * communication failure.
     * @throws SaltStackException if anything goes wrong
     */
    public boolean sendEvent(String eventTag, String eventData) throws SaltStackException {
        String tag = eventTag != null ? eventTag : "";
        Map<String, Object> result = connectionFactory
                .create("/hook/" + tag, JsonParser.MAP, config)
                .getResult(eventData);
        return Boolean.TRUE.equals(result.get("success"));
    }

    /**
     * Asynchronously trigger an event in Salt with the specified tag and data.
     * <p>
     * {@code POST /hook}
     *
     * @param eventTag the event tag
     * @param eventData the event data. Must be valid JSON.
     * @return Future containing a boolean value indicating the success or failure of
     * triggering the event.
     */
    public Future<Boolean> sendEventAsync(final String eventTag, final String eventData) {
        return executor.submit(() -> sendEvent(eventTag, eventData));
    }
}
