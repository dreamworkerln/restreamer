package ru.kvanttelecom.tv.restreamer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Collections;

@Slf4j
public class RestClient {

    private final RestTemplate restTemplate;
    private final RestreamerProperties props;
    private final Client client;

    public RestClient(RestTemplate restTemplate, RestreamerProperties props, Client client) {
        this.restTemplate = restTemplate;
        this.props = props;
        this.client = client;
    }

    // -------------------------------------------------------------------------------------

    private HttpHeaders getHeaders() {
        HttpHeaders result = new HttpHeaders();
        result.add("user-agent", props.getUserAgent());
        result.set(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken());
        return result;
    }

    // --------------------------------------------------------------------------------------

    public ResponseEntity<String> get(String url) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> post(String url, String body) {
        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }


    public ResponseEntity<String> get(String url, HttpHeaders headers) {
        headers.add("user-agent", props.getUserAgent());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public ResponseEntity<String> post(String url, String body, HttpHeaders headers) {
        headers.add("user-agent", props.getUserAgent());
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }


    public ResponseEntity<byte[]> download(String url) {

        HttpHeaders headers = getHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);  // ResponseEntity<byte[]> response
    }

    public File downloadFile(String url, Path path) {

        RequestCallback requestCallback = request -> {
            HttpHeaders headers = getHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
            request.getHeaders().addAll(headers);
        };

        return restTemplate.execute(url, HttpMethod.GET, requestCallback,
            clientHttpResponse -> {
                File file = null;
                if(clientHttpResponse.getStatusCode() == HttpStatus.OK) {
                    file = path.toFile();
                    StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(file));
                }
                return file;
            });
    }
}


/*

    private ResponseEntity<String> get(String url) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", ELTEX_USER_AGENT);

        RequestEntity<?> requestEntity = RequestEntity
            .get(URI.create(url))
            .headers(headers)
            .build();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }



    private void post(String url, String body) {

        HttpHeaders headers = getHeadersWithDefaultUserAgent();

        RequestEntity requestEntity = RequestEntity
            .post(URI.create(url))
            .headers(headers)
            .body(body);


        restTemplate.exchange(requestEntity, String.class);
    }



 */
