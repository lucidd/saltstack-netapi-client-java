package com.suse.saltstack.netapi.calls.runner;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.RunnerCall;
import com.suse.saltstack.netapi.parser.JsonParser;
import com.suse.saltstack.netapi.results.Result;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * salt.runners.jobs
 */
public class Jobs {

    /**
     *
     */
    public static class Info {
        @SerializedName("Function")
        private String function;

        private String jid;

        @SerializedName("StartTime")
        @JsonAdapter(JsonParser.JobStartTimeJsonAdapter.class)
        private Date startTime;

        @SerializedName("Arguments")
        private List<Object> arguments;

        @SerializedName("Minions")
        private Set<String> minions;

        @SerializedName("User")
        private String user;

        @SerializedName("Target")
        private String target;

        @SerializedName("Result")
        private Map<String, Result<Object>> result;

        public String getFunction() {
            return function;
        }

        public String getJid() {
            return jid;
        }

        public Date getStartTime() {
            return startTime;
        }

        public List<Object> getArguments() {
            return arguments;
        }

        public Set<String> getMinions() {
            return minions;
        }

        public String getUser() {
            return user;
        }

        public String getTarget() {
            return target;
        }

        public Map<String, Result<Object>> getResult() {
            return result;
        }

        public Optional<Object> resultOf(String minionKey) {
            return Optional.ofNullable(result.get(minionKey));
        }
    }

    private Jobs() { }

    public static RunnerCall<Info> listJob(String jid) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("jid", jid);
        return new RunnerCall<>("jobs.list_job", Optional.of(args), new TypeToken<Info>() {
        });
    }

    public static RunnerCall<Map<String, Object>> lookupJid(String jid) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("jid", jid);
        return new RunnerCall<>("jobs.lookup_jid", Optional.of(args),
                new TypeToken<Map<String, Object>>(){});
    }

    public static RunnerCall<Map<String, Info>> printJob(String jid) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("jid", jid);
        return new RunnerCall<>("jobs.print_job", Optional.of(args),
                new TypeToken<Map<String, Info>>(){});
    }

}
