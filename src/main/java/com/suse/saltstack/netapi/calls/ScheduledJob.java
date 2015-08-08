package com.suse.saltstack.netapi.calls;

/**
 * Common class representing a scheduled job
 * @param <R> the return type of the called function
 */
public class ScheduledJob<R> {

    private String jid;

    public String getJid() {
        return jid;
    }
}
