package ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RestreamerProperties {

    /**
     * Address of origin streamer or balancer (host:port)
     */
    @Value("${restreamer.origin.streamer.address}")
    @Getter(AccessLevel.PUBLIC)
    private String originAddress;

    /**
     * URL of authorisation backend
     */
    @Value("${restreamer.auth.url}")
    @Getter(AccessLevel.PUBLIC)
    private String authUrl;

    @Value("${restreamer.auth.enabled:true}")
    @Getter(AccessLevel.PUBLIC)
    private Boolean authEnabled;

    /**
     * Playlist URL
     */
    @Value("${restreamer.playlist.url}")
    @Getter(AccessLevel.PUBLIC)
    private String playlistUrl;

    /**
     * Custom useragent
     */
    @Value("${restreamer.useragent}")
    @Getter(AccessLevel.PUBLIC)
    private String userAgent;

    /**
     * Path to store downloaded hls chunks
     */
    @Value("${restreamer.storage.path}")
    @Getter(AccessLevel.PUBLIC)
    private String storagePath;


    /**
     * Path to store playlist (default stored inside storagePath)
     */
    @Value("${restreamer.playlist.path:}")
    @Getter
    @Setter
    private String playlistPath;

    /**
     *  Application host
     *  imported from application.properties server.host
     */
    @Value("${server.host}")
    @Getter(AccessLevel.PUBLIC)
    private String host;


    /**
     *  Application port
     *  imported from application.properties server.port
     */
    @Value("${server.port}")
    @Getter(AccessLevel.PUBLIC)
    private String port;


    /**
     * Get streamer address
     * @return
     */
    public String getAddress(){
        return host + ":" + port;
    }

    @PostConstruct
    private void postConstruct() {
         // remove trailing "/" from storagePath (if exists)
        if (storagePath.endsWith("/")) {
            storagePath = storagePath.substring(0, storagePath.length() - 1);
        }
    }
    
}
