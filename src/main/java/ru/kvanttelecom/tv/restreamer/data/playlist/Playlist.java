package ru.kvanttelecom.tv.restreamer.data.playlist;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
@Data
public class Playlist {

    //public NavigableMap<String, Stream> map = new TreeMap<>();

    public ConcurrentMap<String, Stream> map = new ConcurrentHashMap<>();
    //public ConcurrentNavigableMap<String, Stream> map = new ConcurrentSkipListMap<>();

    @Override
    public String toString() {
        return map.toString();
    }
}
