package com.britesnow.snow.util;

public final class Pair <N, V> {
    private N first;
    private V second;

    public Pair(N name, V value) {
        this.first = name;
        this.second = value;
    }

    public N getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

}