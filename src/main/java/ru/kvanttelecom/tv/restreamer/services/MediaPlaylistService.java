package ru.kvanttelecom.tv.restreamer.services;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.data.masterplaylist.QualityStream;
import ru.kvanttelecom.tv.restreamer.data.mediaplaylist.Chunk;
import ru.kvanttelecom.tv.restreamer.data.mediaplaylist.MediaPlaylist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Stream;
import ru.kvanttelecom.tv.restreamer.utils.Utils;
import ru.kvanttelecom.tv.restreamer.utils.threadpool.BatchItem;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.pivovarit.function.ThrowingSupplier.unchecked;
import static ru.kvanttelecom.tv.restreamer.utils.StringUtils.isBlank;
import static ru.kvanttelecom.tv.restreamer.utils.Utils.SEPR;

@Service
@Slf4j
public class MediaPlaylistService {

    @Autowired
    private RestreamerProperties props;
    @Autowired
    private Playlist playlist;
    @Autowired
    private Utils utils;
    @Autowired
    private RestClient restClient;
    @Autowired
    private ChunkService chunkService;


    public String getMediaPlaylist(String streamName, String quality) {

        String result = null;

        if(playlist.map.containsKey(streamName)) {
            Stream stream = playlist.map.get(streamName);

            if(stream.masterPlaylist.map.containsKey(quality)) {
                QualityStream qualityStream = stream.masterPlaylist.map.get(quality);

                long stamp;

                // update chunklist if expired
                if(qualityStream.mediaPlaylist.isExpired()) {


                    //log.trace("MEDIA PLAYLIST: {}", System.identityHashCode(qualityStream.mediaPlaylist));
                    //log.trace("LOCK NFO: {}", lock);

                    synchronized (qualityStream.mediaPlaylist.lock) {
                        // double-checking, if playlist have been update -> skip it
                        if(qualityStream.mediaPlaylist.isExpired()) {
                            updateMediaPlaylist(qualityStream);
                        }
                    }
                }
                // available unconsistent read
                result = qualityStream.mediaPlaylist.raw;
            }
        }
        return result;
    }


    // ============================================================================================



    /**
     * download and update mediaPlaylist for specified qualityStream
     * may download/delete chunk files
     * Не пускать сюда одновременно более 1 потока
     */
    private void updateMediaPlaylist(QualityStream qualityStream) {

        ResponseEntity<String> response;
        String body;

        try {

            log.trace("DOWNLOADING MEDIA PLAYLIST: {}", qualityStream.url);
            response = restClient.get(qualityStream.url);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.info("QualityStream: {} - disabled by origin: {}", qualityStream.dir, response.getStatusCode().toString());
                return;
            }

            body = response.getBody();

            if (isBlank(body)) {
                log.info("QualityStream: {} - empty body", qualityStream.dir);
                return;
            }

            Assert.notNull(body, "MediaPlaylist is null");
            String[] mediaPlaylistString = body.split("\n");


            MediaPlaylist mpl = qualityStream.mediaPlaylist;

            // assign new raw value with replaced origin streamer
            mpl.raw = utils.replaceStreamerToLocal(body);

            // default MediaPlaylist TTL
            Duration mediaPlaylistTTL = Duration.ofSeconds(6);

            // new downloaded MediaPlaylist
            MediaPlaylist mplNew = new MediaPlaylist();

            // full mplNew
            for (String url : mediaPlaylistString) {

                if (url.contains("EXT-X-TARGETDURATION")) {
                    String[] lst = url.split(":");
                    if(lst.length == 2) {
                        mediaPlaylistTTL = Duration.ofSeconds(Integer.parseInt(lst[1]));
                    }
                    continue;
                }

                if (url.startsWith("#")) {
                    continue;
                }

                URL link = unchecked(() ->new URL(url)).get();
                String path = link.getPath();
                Chunk chunk = new Chunk(url, path); // chunk.exists == false
                mplNew.map.put(path, chunk);
            }

            // Set MediaPlaylist TTL
            mpl.expiredAt = Instant.now().plus(mediaPlaylistTTL);

            // Updating existing MediaPlaylist and downloading chunks -------------------------

            // Need to update existing(old) mpl using mplNew

            // 1. remove chunks from mpl that not exists in mplNew
            for (Map.Entry<String, Chunk> e : mpl.map.entrySet()) {

                if(!mplNew.map.containsKey(e.getKey())) {

                    // remove old chunk files
                    String chunkPath = props.getStoragePath() + SEPR +
                        qualityStream.dir + SEPR +
                        FilenameUtils.getName(e.getValue().path);

                    try {
                        Files.delete(Path.of(chunkPath));
                    } catch (IOException io) {
                        log.error("Deleting chunk - file not found: ", io);
                    }

                    // remove chunk from mpl
                    mpl.map.remove(e.getKey());
                }
            }

            // 2. download & add to mpl chunks that exists only in mplNew
            List<BatchItem<Void, Void>> batchList = new ArrayList<>();

            for (Map.Entry<String, Chunk> e : mplNew.map.entrySet()) {

                if (!mpl.map.containsKey(e.getKey())) {

                    // will not actually start download chunk file,
                    // only assign Function<> used to perform download
                    batchList.add(new BatchItem<>(null,
                        unused -> chunkService.downloadChunk(qualityStream, mpl, e)));
                }
            }
            // start downloading chunk files, blocking call
            chunkService.batchChunks(batchList);
        }
        catch (Throwable e) {
            if(e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                log.info("Chunk: {} - socket read timeout", qualityStream.dir);
            }
            else {
                log.info("Chunk: {} - problem", qualityStream.dir);
                log.error("ERROR:", e);
            }
        }
    }
}



/*



// ConcurrentMap allow remove elements without iterator
//Iterator<Map.Entry<String, Chunk>> it = mpl.map.entrySet().iterator();
//while (it.hasNext()) {
//Map.Entry<String, Chunk> e = it.next();



//            // ждем скачивания всех чанков
//            List<JobResult<Void, Void>> resultList = jobPool.batchWait(batchList);
//
//            // если хотя бы один чанк не скачался - проблемы
//            for(JobResult<Void, Void> rrr : resultList) {
//                if(rrr.getException() != null) {
//                    throw rrr.getException();
//                }
//            }

 */