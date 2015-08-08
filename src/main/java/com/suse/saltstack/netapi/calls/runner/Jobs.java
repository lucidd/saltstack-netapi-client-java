package com.suse.saltstack.netapi.calls.runner;

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.RunnerCall;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Jobs {

   private Jobs(){
   }


   public static RunnerCall<Map<String, Object>> listJob(String jid) {
      LinkedHashMap<String, Object> args = new LinkedHashMap<>();
      args.put("jid", jid);
      return new RunnerCall<>("jobs.list_job", Optional.of(args), new TypeToken<Map<String, Object>>(){});
   }

   public static RunnerCall<Map<String, Object>> lookupJid(String jid) {
      LinkedHashMap<String, Object> args = new LinkedHashMap<>();
      args.put("jid", jid);
      return new RunnerCall<>("jobs.lookup_jid", Optional.of(args), new TypeToken<Map<String, Object>>(){});
   }

   public static RunnerCall<Map<String, Object>> printJob(String jid) {
      LinkedHashMap<String, Object> args = new LinkedHashMap<>();
      args.put("jid", jid);
      return new RunnerCall<>("jobs.print_job", Optional.of(args), new TypeToken<Map<String, Object>>(){});
   }

}
