package com.zpj.bus;

public interface Consumer<T> {
    void accept(T t);
}
