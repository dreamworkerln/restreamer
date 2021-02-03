package ru.kvanttelecom.tv.restreamer.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.services.ChunkService;
import ru.kvanttelecom.tv.restreamer.services.MasterPlaylistService;
import ru.kvanttelecom.tv.restreamer.services.MediaPlaylistService;
import ru.kvanttelecom.tv.restreamer.services.PlaylistService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ru.kvanttelecom.tv.restreamer.utils.StringUtils.notBlank;

@RestController
@Slf4j
public class StreamController {

    @Autowired
    Playlist playlist;
    @Autowired
    PlaylistService playlistService;
    @Autowired
    private RestreamerProperties props;
    @Autowired
    private MasterPlaylistService masterPlaylistService;
    @Autowired
    private MediaPlaylistService mediaPlaylistService;
    @Autowired
    private ChunkService chunkService;


    /**
     * Master playlist request
     * @param stream name (http://streamer.com:8080/mir24_hd/index.m3u8)
     * @return MasterPlaylist - list of QualityStream
     */
    @GetMapping(value = "/{stream}/index.m3u8")
    public ResponseEntity<String> getMasterPlaylist(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    @PathVariable("stream") String stream) {

        ResponseEntity<String> result = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        try {

            log.trace("GET: " + request.getRequestURI());

            String masterPlaylist = masterPlaylistService.getMasterPlaylist(stream);
            if(notBlank(masterPlaylist)) {
                result = ResponseEntity.ok(masterPlaylist);
                response.setContentType("application/vnd.apple.mpegurl");
            }
        }
        catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            log.error("SERVER ERROR:", e);
        }

        log.trace("RESULT: " + result.getStatusCode());
        return result;
    }


    /**
     * MediaPlaylist request (Chunklist)
     * @param stream name and quality variant (http://streamer.com:8080/mir24_hd/tracks-v3a1/mono.m3u8)
     * @return MediaPlaylist - list of Chunks
     */
    @GetMapping(value = "/{stream}/{quality}/mono.m3u8")
    public ResponseEntity<String> getMediaPlaylist(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   @PathVariable("stream") String stream,
                                                   @PathVariable("quality") String quality) {

        ResponseEntity<String> result = ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        try {

            log.trace("GET: " + request.getRequestURI());

            String mediaPlaylist = mediaPlaylistService.getMediaPlaylist(stream, quality);
            if(notBlank(mediaPlaylist)) {
                result = ResponseEntity.ok(mediaPlaylist);
            }
            response.setContentType("application/vnd.apple.mpegurl");

        }
        catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            log.error("SERVER ERROR:", e);
        }

        log.trace("RESULT: " + result.getStatusCode());
        return result;
    }



    /**
     * Chunk request
     * @param chunk url (http://streamer.com:8080/mir24_hd/tracks-v1a1/2021/01/21/16/56/32-06000.ts)
     * @return octet stream
     */
    @GetMapping(value = "/{stream}/{quality}/{p2}/{p3}/{p4}/{p5}/{p6}/{chunk}")
    public ResponseEntity<?> getChunk(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("stream") String stream,
                         @PathVariable("quality") String quality,
                         @PathVariable("chunk") String chunk) {

        ResponseEntity<?> result = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            String requestPath = request.getRequestURI();
            log.trace("GET: " + requestPath);

            if(chunkService.getChunk(requestPath, stream, quality, chunk, response)) {
                result = ResponseEntity.ok().build();
            }
        }
        catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            log.error("SERVER ERROR:", e);
        }
        log.trace("RESULT: " + result.getStatusCode());
        return result;
    }

}


