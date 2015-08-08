package com.suse.saltstack.netapi.calls;

import java.util.List;

public class LocalAsyncResult<R> extends ScheduledJob<R> {

    private List<String> minions;

    public List<String> getMinions() {
        return minions;
    }
}
