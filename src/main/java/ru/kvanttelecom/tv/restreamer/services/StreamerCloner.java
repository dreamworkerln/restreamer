package ru.kvanttelecom.tv.restreamer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;
import ru.kvanttelecom.tv.restreamer.services.auth.AuthService;

import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
@Slf4j
public class StreamerCloner {



    // AtomicLong channelInProgress = new AtomicLong(0);

    @Autowired
    private Client client;
    @Autowired
    private AuthService authService;
    @Autowired
    private PlaylistService playlistService;
    @Autowired
    private RestreamerProperties props;
    @Autowired
    private Playlist playlist;

    public void start() throws IOException {

        log.info("INITIALIZATION =======================================================");
        if(props.getAuthEnabled()) {
            log.info("CHECKING USER: {}", client);

            if (!authService.validate(client)) {
                throw new AccessDeniedException("Access denied for user: " + client);
            }
        }

        log.info("RESTREAMING ORIGIN: {}", props.getOriginAddress());

        log.info("GETTING PLAYLIST:"); // SET STREAMER_ADDRESS TO USE AS SOURCE STREAMER
        playlistService.loadPlayList();

        log.info("CLEANUP HLS DIR");
        cleanupHlsPath();

        log.info("INITIALIZATION COMPLETE ==============================================");
    }

    // =======================================================================================


    /**
     * Delete all dirs from props.storagePath
     * <br>exclude: props.playlistPath (playlist file)
     */
    public void cleanupHlsPath() {
        //Files.delete(Path.of(props.getStoragePath()));

        Path start = Path.of(props.getStoragePath());
        Path playlistPath = Path.of(props.getPlaylistPath());

        try {
            Files.walk(start)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(file -> {
                    if(!file.getPath().equals(start.toString()) &&
                        !file.getPath().equals(playlistPath.toString())) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                });
        } catch (IOException e) {
            log.error("CLEANUP ERROR: ", e);
        }
    }
}



/*

//    /**
//     * Get playlist
//
//    private void refreshPlaylist() {
//
//        log.info("GET NEW PLAYLIST ================================================");
//
//        playList = playListService.getPlayList();
//    }




    private void createMasterPlaylistDirs() throws IOException {

        for(Map.Entry<String, Stream> entry : playlist.map.entrySet()) {

            Stream stream = playlist.map.get(entry.getKey());

            for(QualityStream item: stream.masterPlaylist) {

                // local filesystem full path
                Path localPath = Path.of(props.getStoragePath() + File.separator + item.dir);

                if (Files.notExists((localPath))) {
                    Files.createDirectories(localPath);
                }
            }
        }
    }
















        //log.info("STARTING LOOP ===================================================");



        // 1st time executed in main thread, next time will executed in BlockingJobPool.ThreadPoolExecutor
//        mainLoop();

//        // waiting for SIGTERM/SIGKILL
//        while(!jobPool.isTerminated()) {
//            try {
//                jobPool.awaitTermination(1, TimeUnit.SECONDS);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }






//    private void createMasterPlaylistDirs() throws IOException {
//
//        for(Map.Entry<String, Stream> entry : playlist.map.entrySet()) {
//
//            Stream stream = playlist.map.get(entry.getKey());
//
//            for(QualityStream item: stream.masterPlaylist) {
//
//                // local filesystem full path
//                Path localPath = Path.of(props.getStoragePath() + File.separator + item.dir);
//
//                if (Files.notExists((localPath))) {
//                    Files.createDirectories(localPath);
//                }
//            }
//        }
//    }

                    if (Files.exists(path)) {
                        response.setContentLength((int)Files.size(path));
                        response.setContentType("video/MP2T");
                        //response.addHeader("Content-Disposition", "attachment; filename=" + chunk);

                        Files.copy(path, response.getOutputStream());
                        response.getOutputStream().flush();
                    }

 */




