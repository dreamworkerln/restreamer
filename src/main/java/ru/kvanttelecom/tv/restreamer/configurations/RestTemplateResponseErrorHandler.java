package ru.kvanttelecom.tv.restreamer.configurations;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

/**
 * Disable exception throwing on SERVER and CLIENT errors
 */
@Component
public class RestTemplateResponseErrorHandler
    implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
        throws IOException {

        return (
            httpResponse.getStatusCode().series() == CLIENT_ERROR
                || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    // Suppress throwing SERVER and CLIENT errors
    @Override
    public void handleError(ClientHttpResponse httpResponse)
        throws IOException {

        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR ||
            httpResponse.getStatusCode().series() == CLIENT_ERROR) {
            // suppress
        }

//        if (httpResponse.getStatusCode()
//            .series() == HttpStatus.Series.SERVER_ERROR) {
//            // handle SERVER_ERROR
//        }
//        else if (httpResponse.getStatusCode()
//            .series() == HttpStatus.Series.CLIENT_ERROR) {
//            // handle CLIENT_ERROR
//            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//                //throw new NotFoundException();
//            }
//        }
        }
    }
