package ru.kvanttelecom.tv.restreamer.services;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.data.masterplaylist.QualityStream;
import ru.kvanttelecom.tv.restreamer.data.mediaplaylist.Chunk;
import ru.kvanttelecom.tv.restreamer.data.mediaplaylist.MediaPlaylist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Stream;
import ru.kvanttelecom.tv.restreamer.utils.Utils;
import ru.kvanttelecom.tv.restreamer.utils.threadpool.BatchItem;
import ru.kvanttelecom.tv.restreamer.utils.threadpool.BlockingJobPool;
import ru.kvanttelecom.tv.restreamer.utils.threadpool.JobResult;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.kvanttelecom.tv.restreamer.utils.Utils.SEPR;

@Service
@Slf4j
public class ChunkService {

    private final AtomicInteger chunkRequests = new AtomicInteger();


    private final BlockingJobPool<Void, Void> jobPool =
        new BlockingJobPool<>(10, Duration.ofMillis(4000), unused -> {});


    @Autowired
    private RestreamerProperties props;
    @Autowired
    private Playlist playlist;
    @Autowired
    private Utils utils;
    @Autowired
    @Qualifier("restClientWithException")
    private RestClient restClient;


    /**
     * Write chunk bytes HttpServletResponse OutputStream
     * <br> Will do it only with fully downloaded (consisted) chunks.
     *
     * <br> Chunks that been downloaded are added to mediaPlaylist only after they have been fully downloaded
     */
    public boolean getChunk(String requestPath, String streamName, String quality, String chunkName, HttpServletResponse response) {

        boolean result = false; // is chunk available

        if(playlist.map.containsKey(streamName)) {
            Stream stream = playlist.map.get(streamName);

            if (stream.masterPlaylist.map.containsKey(quality)) {
                QualityStream qualityStream = stream.masterPlaylist.map.get(quality);

                if(qualityStream.mediaPlaylist.map.containsKey(requestPath)) {

                    Chunk chunk = qualityStream.mediaPlaylist.map.get(requestPath);

                    Path path = Paths.get(props.getStoragePath() + SEPR +
                        qualityStream.dir + SEPR + chunkName);

                    try (InputStream inputStream = new FileInputStream(path.toFile())) {

                        //Files.copy(path, response.getOutputStream());
                        long fileSize = inputStream.transferTo(response.getOutputStream());
                        response.setContentType("video/MP2T");
                        response.setContentLength((int) fileSize);
                        response.getOutputStream().flush();
                        result = true;
                        chunkRequests.incrementAndGet();
                    } catch (FileNotFoundException ignore) {
                        log.trace("File not found: {}", path.toString());
                    } catch (Exception e) {
                        log.error("ERROR: ", e);
                    }
                }
                // DEBUG
                else {
                    log.trace("Chunk not found: {}", requestPath);
                    log.trace("Available chunks: {}", qualityStream.mediaPlaylist.map.values().toString());
                }
            }
        }
        return result;
    }


    /**
     * get and reset chunk download request count (Monitoring)
     * @return int
     */
    public int pollChunkRequests() {
        return chunkRequests.getAndSet(0);
    }


    /**
     * Start download chunks
     */
    public void batchChunks(List<BatchItem<Void, Void>> batchList) throws ExecutionException, InterruptedException {

        // download in jobPool threads(if jobPool thread pool is full then in current thread)
        jobPool.batch(batchList);
    }


    /**
     * Function<> that used to perform download chunk file
     * <br> Chunks that been downloaded are added to mediaPlaylist only after they have been fully downloaded
     * <br> So if chunk is been downloading client will receive 404 respond
     */
    @SneakyThrows
    public JobResult<Void,Void> downloadChunk(QualityStream qualityStream, MediaPlaylist mpl, Map.Entry<String, Chunk> entry) {

        try {

            Chunk chunk = entry.getValue();

            Path mediaPlaylistPath = Path.of(props.getStoragePath() + SEPR + qualityStream.dir);
            Path chunkPath = Path.of(mediaPlaylistPath.toString() + SEPR +
                FilenameUtils.getName(chunk.path));

            // Create dir
            if(Files.notExists(mediaPlaylistPath)) {
                Files.createDirectories(mediaPlaylistPath);
            }

            log.trace("DOWNLOADING CHUNK: {} to {}", chunk.url, chunkPath);

            // download chunk to file
            try {
                // first download chunk to local file
                File file = restClient.downloadFile(chunk.url, chunkPath);

                // not needed
                if(file == null) {
                    throw new IllegalArgumentException("ERROR CHUNK ' " + chunk.url + " ' NOT FOUND ON ORIGIN");
                }

                // then add fully downloaded chunk to MediaPlaylist
                mpl.map.put(entry.getKey(), chunk);

                log.trace("DOWNLOADING COMPLETE: {} ", chunkPath);
            }
            catch (Throwable e) {
                if(e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                    log.info("Chunk download: {} - socket read timeout", qualityStream.dir);
                }
                else {
                    log.info("Chunk download: {} - problem", qualityStream.dir);
                    log.error("ERROR:", e);
                }
            }
        }
        catch (Exception e) {
            log.error("Chunk download error: ??? ", e);
            //throw e;
        }
        // do not need results
        return new JobResult<>();
    }


    // =================================================================================

    private void handleResults(JobResult<Stream,Void> jobResult) {
        log.info("Job done: {}", jobResult.getArgument());
    }


}


/*

                    try {
                        int cnt = 0;
                        // wait until chunk finish to downloading or something go wrong
                        while(!chunk.exists && cnt < 10) {
                            log.trace("WAITING CHUNK TO BE DOWNLOADED");
                            cnt++;
                            TimeUnit.MILLISECONDS.sleep(500);
                        }
                    }


                    // intended ignoring InterruptedException, just stop waiting
                    // catch (InterruptedException ignore) {}



 */