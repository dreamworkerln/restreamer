package ru.kvanttelecom.tv.restreamer.data.mediaplaylist;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Instant;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Chunk list
 * ToDo: написать обертку над map
 */
public class MediaPlaylist {

    //AtomicReference<Instant> ttt = new AtomicReference<>();

    // key = chunk path
    // key = mir24_hd/tracks-v1a1/2021/01/21/16/56/32-06000.ts
    public ConcurrentNavigableMap<String, Chunk> map = new ConcurrentSkipListMap<>();

    // TTL берется с TTL чанков
    public volatile Instant expiredAt = Instant.EPOCH;

    public String raw;

    public final Object lock = new Object();

    @JsonIgnore
    public boolean isExpired() {
        return Instant.now().isAfter(expiredAt);
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
