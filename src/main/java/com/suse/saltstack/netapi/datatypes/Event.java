package com.suse.saltstack.netapi.datatypes;

import java.util.HashMap;

public class Event {

    private String tag;
    private HashMap<String, Object> data;

    public HashMap getData() {
        return data;
    }

    public String getTag() {
        return tag;
    }
}
