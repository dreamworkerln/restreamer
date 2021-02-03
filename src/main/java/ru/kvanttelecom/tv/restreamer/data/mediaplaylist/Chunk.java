package ru.kvanttelecom.tv.restreamer.data.mediaplaylist;


import java.util.concurrent.locks.StampedLock;

public class Chunk {

    // url on origin streamer
    public String url;

    // local relative path
    public String path;

    public Chunk(String url, String path) {
        this.url = url;
        this.path = path;
    }

    @Override
    public String toString() {
        return "Chunk{" +
            "path='" + path + '\'' +
            '}';
    }
}
