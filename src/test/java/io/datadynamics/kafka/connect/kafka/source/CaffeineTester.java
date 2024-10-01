package io.datadynamics.kafka.connect.kafka.source;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class CaffeineTester {

    public static void main(String[] args) {
        Caffeine<String, String> caffeine = build();
        Cache<String, String> cache = caffeine.build();

        cache.put("A", "B");

        System.out.println(cache.getIfPresent("A"));
    }

    public static Caffeine build() {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .expireAfterWrite(12, TimeUnit.HOURS)
                .maximumSize(100000);
        return caffeine;
    }
}
