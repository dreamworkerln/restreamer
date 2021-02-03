package ru.kvanttelecom.tv.restreamer.validators;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kvanttelecom.tv.restreamer.data.playlist.Playlist;
import ru.kvanttelecom.tv.restreamer.data.playlist.Stream;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static ru.kvanttelecom.tv.restreamer.utils.StringUtils.isBlank;

@Component
@Slf4j
public class StreamValidator {

    @Autowired
    private Validator validator;

    UrlValidator urlValidator = new UrlValidator();

    public void validatePlaylist(Playlist playlist) {
        for(Map.Entry<String, Stream> entry : playlist.map.entrySet()) {
            validate(entry.getValue());
        }
    }

    private void validate(Stream stream) {

        String id = stream.name;
        if(isBlank(id)) {
            id = "{null, " + stream.displayName + ", " + stream.url + "}";
        }

        Set<ConstraintViolation<Stream>> violations = validator.validate(stream);
        if (violations.size() != 0) {
            throw new ConstraintViolationException("Stream: '" + id + "' validation failed", violations);
        }

        // custom checks url
        try {
            new URL(stream.url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Stream: '" + id + "' url " + stream.url +  " validation failed", e);
        }
        if (!urlValidator.isValid(stream.url)) {
            throw new IllegalArgumentException("Stream: '" + id + "' url " + stream.url +  " validation failed");
        }
    }
}
