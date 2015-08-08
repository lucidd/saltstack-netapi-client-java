package com.suse.saltstack.netapi.calls;

import com.google.gson.reflect.TypeToken;

public class WheelAsyncResult<R> extends ScheduledJob<R> {

    private String tag;

    public String getTag() {
        return tag;
    }
}
