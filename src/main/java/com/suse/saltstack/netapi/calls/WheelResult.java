package com.suse.saltstack.netapi.calls;


import com.google.gson.annotations.SerializedName;

public class WheelResult<R> {

    public class Data<R> {

        private String jid;
        @SerializedName("return")
        private R result;
        private boolean success;
        private String tag;
        private String user;
        private String fun;

        public String getJid() {
            return jid;
        }

        public R getResult() {
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getTag() {
            return tag;
        }

        public String getUser() {
            return user;
        }

        public String getFun() {
            return fun;
        }
    }

    private String tag;
    private Data<R> data;

    public Data<R> getData() {
        return data;
    }

    public String getTag() {
        return tag;
    }
}
