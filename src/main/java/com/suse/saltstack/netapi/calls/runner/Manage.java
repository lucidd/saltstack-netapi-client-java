package com.suse.saltstack.netapi.calls.runner;

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.RunnerCall;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class Manage {

   private Manage(){}


   public static RunnerCall<List<String>> down(boolean removekeys) {
      LinkedHashMap<String, Object> args = new LinkedHashMap<>();
      args.put("removekeys", removekeys);
      return new RunnerCall<>("manage.down", Optional.of(args), new TypeToken<List<String>>(){});
   }

   public static RunnerCall<List<String>> up() {
      return new RunnerCall<>("manage.up", Optional.empty(), new TypeToken<List<String>>(){});
   }

}
