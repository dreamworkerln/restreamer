package ru.kvanttelecom.tv.restreamer.data.masterplaylist;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * MediaPlaylist list
 * ToDo: написать обертку над map
 */
public class MasterPlaylist {

   public static final Duration TTL = Duration.ofMinutes(10);

    @JsonIgnore
   public final Object lock = new Object();

   public ConcurrentNavigableMap<String, QualityStream> map = new ConcurrentSkipListMap<>();

   public volatile Instant expiredAt = Instant.EPOCH;

   public String raw;

   // CAS, publication, safe publication ... need to learn this
   // https://shipilev.net/blog/2014/safe-public-construction/#_safe_publication
   @JsonIgnore
   public boolean isExpired() {
       return Instant.now().isAfter(expiredAt);
   }

    @Override
    public String toString() {
        return map.toString();
    }

}



//ConcurrentNavigableMap<String, Stream> map = new ConcurrentSkipListMap<>();
// public Queue<MPLItem> queue = new ConcurrentLinkedQueue<>()
