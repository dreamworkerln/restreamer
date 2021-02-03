package ru.kvanttelecom.tv.restreamer.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.data.masterplaylist.MasterPlaylist;
import ru.kvanttelecom.tv.restreamer.data.masterplaylist.QualityStream;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Stream;
import ru.kvanttelecom.tv.restreamer.utils.Utils;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.pivovarit.function.ThrowingSupplier.unchecked;
import static ru.kvanttelecom.tv.restreamer.utils.StringUtils.isBlank;

@Service
@Slf4j
public class MasterPlaylistService {

    @Autowired
    RestreamerProperties props;
    @Autowired
    Playlist playlist;
    @Autowired
    Utils utils;
    @Autowired
    private RestClient restClient;


    public String getMasterPlaylist(String streamName) {

        String result = null;

        if(playlist.map.containsKey(streamName)) {
            Stream stream = playlist.map.get(streamName);

            // update master playlist if expired
            if(stream.masterPlaylist.isExpired()) {

                //log.trace("MASTER PLAYLIST: {}", System.identityHashCode(stream.masterPlaylist));
                //log.trace("LOCK NFO: {}", lock);

                synchronized (stream.masterPlaylist.lock) {
                    // double-checking, if playlist have been update -> skip it

                    if(stream.masterPlaylist.isExpired()) {
                        updateMasterPlaylist(stream);
                    }
                }
            }
            // available unconsistent read
            result = stream.masterPlaylist.raw;
        }
        return result;
    }



    /**
     * Download and update master playlist for specified stream
     * Не пускать сюда одновременно более 1 потока
     */
    public void updateMasterPlaylist(Stream stream) {

        ResponseEntity<String> response;
        String body;

        try {

            log.trace("DOWNLOADING MASTER PLAYLIST: {}", stream.url);

            response = restClient.get(stream.url);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.info("Stream: {} - disabled by origin: {}", stream.name, response.getStatusCode().toString());
                return;
            }

            body = response.getBody();

            if (isBlank(body)) {
                log.info("Stream: {} - empty body", stream.name);
                return;
            }

            Assert.notNull(body, "Playlist is null");
            String[] masterPlaylistString = body.split("\n");

            MasterPlaylist mpl = stream.masterPlaylist;

            // Clear MasterPlaylist
            mpl.map.clear();
            //log.trace("CLEAR MASTERPLAYLIST !!!!!!!!!!!!!!");

            // assign new raw value with replaced origin streamer
            mpl.raw = utils.replaceStreamerToLocal(body);

            //set expire
            mpl.expiredAt = Instant.now().plus(MasterPlaylist.TTL);

            final String regex = "//.+?/.+?/(.+?)/";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

            for (String url : masterPlaylistString) {
                if(url.startsWith("#")) {
                    continue;
                }
                // qualityUrl == http://streamer.com:8080/mir24_hd/tracks-v3a1/mono.m3u8

                Matcher matcher = pattern.matcher(url);
                String quality = null;
                while (matcher.find()) {
                    if (matcher.groupCount() > 0) {
                        quality = matcher.group(1);
                    }
                }
                URL link = unchecked(() ->new URL(url)).get();
                QualityStream qs = new QualityStream(quality, url);

                // qs.dir == mir24_hd/tracks-v3a1
                qs.dir = FilenameUtils.getPathNoEndSeparator(link.getPath());

                mpl.map.put(qs.quality, qs);
            }

            if(mpl.map.size() == 0) {
                log.info("Stream: {} - empty master playlist", stream.name);
            }
        }
        catch (Exception e) {
            if(e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                log.info("Stream: {} - socket read timeout", stream.name);
            }
            else {
                log.info("Stream: {} - problem", stream.name);
                log.error("ERROR:", e);
            }
        }
    }
}
