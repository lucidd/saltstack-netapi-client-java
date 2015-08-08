package com.suse.saltstack.netapi.calls;

import java.util.*;

public interface Call<R> {

    Map<String, Object> payload();

}
