package com.suse.saltstack.netapi.calls;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WheelCall<R> implements Call<R> {

    private final String functionName;
    private final Optional<Map<String, Object>> kwargs;

    private final TypeToken<R> returnType;


    public WheelCall(String functionName, Optional<Map<String, Object>> kwargs, TypeToken<R> returnType) {
        this.functionName = functionName;
        this.kwargs = kwargs;
        this.returnType = returnType;
    }

    public TypeToken<R> getReturnType() {
        return returnType;
    }

    public Map<String, Object> payload() {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("fun", functionName);
        kwargs.ifPresent(payload::putAll);
        return payload;
    }


}
