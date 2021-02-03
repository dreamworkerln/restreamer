package ru.kvanttelecom.tv.restreamer.data.playlist;

import ru.kvanttelecom.tv.restreamer.data.masterplaylist.MasterPlaylist;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Channel on streamer
 */
public class Stream {

   // public static Stream EMPTY = new Stream();

    /**
     * Channel name
     */
    @NotNull
    public String name;

    /**
     * Localized name
     */
    public String displayName;

    /**
     * Stream origin url
     * <br>http://balancer/mir24_hd/index.m3u8
     */
    @NotNull
    public String url;


    /**
     * Master playlist
     */
    public MasterPlaylist masterPlaylist = new MasterPlaylist();


    @Override
    public String toString() {
        return name;
    }
}
