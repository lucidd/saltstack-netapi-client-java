package com.suse.saltstack.netapi.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;

public class Try<T> {

   Optional<Throwable> error = Optional.empty();
   Optional<T> value = Optional.empty();

    private Try(Throwable error){
        this.error = Optional.ofNullable(error);
    }

    private Try(T value){
        this.value = Optional.ofNullable(value);
    }

    public static <T> Try<T> of(Supplier<T> fn){
        try {
            T value = fn.get();
            return new Try<T>(value);
        } catch (Throwable t) {
            return new Try<T>(t);
        }
    }

}
