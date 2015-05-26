package com.suse.saltstack.netapi.parser;

import java.io.*;

public interface Parser<T> {
    T parse(InputStream inputStream);
}
