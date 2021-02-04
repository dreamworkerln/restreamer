package ru.kvanttelecom.tv.restreamer.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Stream;
import ru.kvanttelecom.tv.restreamer.utils.Utils;
import ru.kvanttelecom.tv.restreamer.validators.StreamValidator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.kvanttelecom.tv.restreamer.utils.Utils.SEPR;
import static ru.kvanttelecom.tv.restreamer.utils.StringUtils.isBlank;

@Service
@Slf4j
public class PlaylistService {

    private static final String PLAYLIST_FILENAME = "playlist.json";

    @Autowired
    Playlist playlist;
    @Autowired
    private RestClient restClient;
    @Autowired
    private Client client;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RestreamerProperties props;
    @Autowired
    private StreamValidator streamValidator;
    @Autowired
    Utils utils;


    /**
     * Load playlist from file(located in props.getStoragePath()),
     *  if playlist not exists load it from web
     * @return Playlist
     */
    public void loadPlayList() throws IOException {

        boolean usedCache = false;

        props.setPlaylistPath(props.getStoragePath() + SEPR + PLAYLIST_FILENAME);

        Path playlistPath = Path.of(props.getPlaylistPath());

        if (Files.exists(playlistPath) && Files.size(playlistPath) > 0) {
            try {

                TypeReference<ConcurrentMap<String, Stream>> typeRef = new TypeReference<>() {};


//                // copy data from tmp object fields to singleton playlist
//                // Если не копировать поля, а заменить ссылку, то
//                // в остальных бинах в системе ссылка останется старая,
//                // это надо бегать по всем бинам, которые используют
//                // бин playlist и заменять его в них,
//                // либо делать ** двойную(вложенную) ссылку в playlist и менять внутреннее поле, нахрен надо оба варианта
//                BeanUtils.copyProperties(tmp, playlist);

                playlist.map = objectMapper.readValue(playlistPath.toFile(), typeRef);


                streamValidator.validatePlaylist(playlist);
                usedCache = true;
                log.info("PLAYLIST WAS LOADED FROM CACHE");
            }
            catch (Exception e) {
                log.error("PLAYLIST IMPORT ERROR:", e);
            }
        }

        if(!usedCache) {

            log.info("DOWNLOADING NEW PLAYLIST");
            // download playlist
            ResponseEntity<String> response = restClient.get(props.getPlaylistUrl());

            JSONArray list;

            String body = response.getBody();
            if (isBlank(body)) {
                throw new IllegalArgumentException("get Playlist - returns empty");
            }
            // fixing Intellij Idea static code analyzer
            Assert.notNull(body, "Playlist is null");


            // VIDEO LAN playlist
            if (body.contains("videolan.org")) {
                list = XML.toJSONObject(body).getJSONObject("playlist").getJSONObject("trackList").getJSONArray("track");
            }
            // APP playlist
            else {

                list = XML.toJSONObject(body)
                    .getJSONObject("categorized_playlist").getJSONObject("playlist").getJSONArray("track");
            }

            final String regex = "//.+?/(.+?)/";
            final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);


            for (int i = 0; i < list.length(); i++) {
                JSONObject jsonChannel = list.getJSONObject(i);
                Stream stream = new Stream();
                stream.displayName = jsonChannel.getString("title");
                String url = jsonChannel.getString("location");

                // playlist contains balancer address for streams by default
                // replace it to origin streamer address
                URL link = new URL(url);
                int port = link.getPort();
                String address = port > 0 ? link.getHost() + ":" + port : link.getHost();
                stream.url = url.replace(address, props.getOriginAddress());

                Matcher matcher = pattern.matcher(stream.url);
                stream.name = null;
                while (matcher.find()) {
                    if (matcher.groupCount() > 0) {
                        stream.name = matcher.group(1);
                    }
                }

                playlist.map.put(stream.name, stream);
            }

            streamValidator.validatePlaylist(playlist);
            log.info("PLAYLIST WAS DOWNLOADED");

            // apply regional channels
            applyRegionChannels(playlist);

            // save playlist to file
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(playlistPath.toFile(), playlist.map);
        }
    }


    // ================================================================



    private void applyRegionChannels(Playlist playlist) {

        String[] tmp = new String[] {"1kanal","russia1","matctv","ntv","5kanal","russiak","russia24",
            "karusel","otr","tvc","ren","spas","sts","home","tv3","piatnica","zvezda","mir","tnt","muztv"};

        String[] regions = new String[] {"voronezh", "lipeck", "belgorod"};


        Set<String> federalChannels = new HashSet<>(Arrays.asList(tmp));
        Playlist tmpPlaylist = new Playlist();

        for (Map.Entry<String, Stream> entry : playlist.map.entrySet()) {

            Stream stream = entry.getValue();

            if (federalChannels.contains(stream.name)) {

                for(String region : regions) {

                    Stream tmpStream = new Stream();
                    tmpStream.name = stream.name + "_" + region;
                    tmpStream.url = stream.url.replace(stream.name, tmpStream.name);
                    tmpStream.displayName = stream.displayName + "_" + region;

                    tmpPlaylist.map.put(tmpStream.name, tmpStream);
                }
            }
        }

        for (Map.Entry<String, Stream> entry : tmpPlaylist.map.entrySet()) {
            playlist.map.put(entry.getKey(), entry.getValue());
        }
    }
}
/*

//URL uri = unchecked(() -> new URL(channel.url)).get();








///**
//     * Get playlist from origin streamer
//     * @param originStreamerAddress origin streamer to use to copy streams from
//     * @return Playlist

public Playlist getPlayList(@NotNull String originStreamerAddress) throws IOException {

    Playlist result = new Playlist();

    //ToDo: remove after debugging
    boolean useCache = true;


    if(useCache) {

        //result = objectMapper.readValue(new URL("file:playlist.json"), Playlist.class);
        result = objectMapper.readValue(new File("playlist.json"), Playlist.class);
    }
    else {

        // get channels from playlist
        ResponseEntity<String> response = restClient.get(props.getPlaylistUrl());

        JSONArray list;

        String body = response.getBody();
        if (isBlank(body)) {
            throw new IllegalArgumentException("Playlist is empty");
        }
        // fixing Intellij Idea static code analyzer
        Assert.notNull(body, "Playlist is null");


        // VIDEO LAN playlist
        if (body.contains("videolan.org")) {
            list = XML.toJSONObject(body).getJSONObject("playlist").getJSONObject("trackList").getJSONArray("track");
        }
        // APP playlist
        else {

            list = XML.toJSONObject(body)
                .getJSONObject("categorized_playlist").getJSONObject("playlist").getJSONArray("track");
        }

        final String regex = "//.+?/(.+?)/";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);

        for (int i = 0; i < list.length(); i++) {
            JSONObject jsonChannel = list.getJSONObject(i);
            Stream stream = new Stream();
            stream.displayName = jsonChannel.getString("title");
            stream.sourceURL = jsonChannel.getString("location");

            Matcher matcher = pattern.matcher(stream.sourceURL);
            stream.name = null;
            while (matcher.find()) {
                if (matcher.groupCount() > 0) {
                    stream.name = matcher.group(1);
                }
            }

            result.map.put(stream.name, stream);
        }

        // apply regional channels
        applyRegionChannels(result);


        // replace balancer address to specified streamer address
        if (!isBlank(originStreamerAddress)) {
            for (Map.Entry<String, Stream> entry : result.map.entrySet()) {

                Stream stream = entry.getValue();
                URL uri = new URL(stream.sourceURL);
                int port = uri.getPort();
                String address = port > 0 ? uri.getHost() + ":" + port : uri.getHost();
                stream.sourceURL = stream.sourceURL.replace(address, props.getAddress());
            }
        }


        // load master playlists
        setMasterPlaylists(result);


        // remove unavailable channels (without master playlist)
        // result.map.entrySet().removeIf(e -> e.getValue().masterPlaylist.size() == 0);


        // save to cache
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File("playlist.json"), result);
    }

    return result;
}












//    /**
//     * Get new master playlist for all channels from playList & update channel.stUrl

private void setMasterPlaylists(Playlist playlist) {

    ResponseEntity<String> response;
    String body;

    for(Map.Entry<String, Stream> entry : playlist.map.entrySet()) {

        Stream stream = Stream.EMPTY;
        try {
            stream = entry.getValue();

            // Go to balancer and thru it's redirect to streamer -------------------------------------------------------
            // and get master playlist
            String balancerUrl = stream.sourceURL +
                "?token=" +
                user.getUid() +
                "-" +
                user.getToken() +
                "-" +
                getEltexTimeStamp();

            response = restClient.get(balancerUrl);

            if (response.getStatusCode() != HttpStatus.OK) {
                log.info("Channel: {} - disabled by balancer: {}", stream.name, response.getStatusCode().toString());
                continue;
            }

            body = response.getBody();

            if (isBlank(body)) {
                log.info("Channel: {} - empty body", stream.name);
                continue;
            }

            Assert.notNull(body, "Playlist is null");
            String[] masterPlaylistString = body.split("\n");

            stream.masterPlaylist = Arrays.stream(masterPlaylistString).
                map(MPLItem::new)
                .collect(Collectors.toList());

            stream.masterPlaylist.removeIf(i -> i.url.startsWith("#"));
            if(stream.masterPlaylist.size() == 0) {
                log.info("Channel: {} - empty master playlist", stream.name);
            }

            for(MPLItem item: stream.masterPlaylist) {

                URL uri = unchecked(() ->new URL(item.url)).get();
                // get parent path
                item.dir = FilenameUtils.getPath(uri.getPath());
            }

        } catch (Exception e) {
            if(e.getCause() != null && e.getCause().getClass() == SocketTimeoutException.class) {
                log.info("Channel: {} - socket read timeout", stream.name);
            }
            else {
                log.info("Channel: {} - problem", stream.name);
            }
        }
    }
}





 */