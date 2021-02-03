package ru.kvanttelecom.tv.restreamer.data.masterplaylist;

import ru.kvanttelecom.tv.restreamer.data.mediaplaylist.MediaPlaylist;


/**
 * Master playlist item, contains MediaPlaylist
 */
public class QualityStream {

    /**
     * Stream name
     */
    public String quality;

    /**
     * Origin url of stream like
     * <br>http://streamer.com:8080/mir24_hd/tracks-v3a1/mono.m3u8
     */
    public String url;

    /**
     * Stream relative dir on local server (/mir24_hd/tracks-v3a1)
     * relative to RestreamerProperties.storagePath
     */
    public String dir;

    /**
     * Chunk list
     */
    public MediaPlaylist mediaPlaylist = new MediaPlaylist();

    public QualityStream() {}

    public QualityStream(String url) {
        this.url = url;
    }

    public QualityStream(String quality, String url) {
        this.quality = quality;
        this.url = url;
    }
}
