package ru.kvanttelecom.tv.restreamer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.kvanttelecom.tv.restreamer.services.StreamerCloner;

@Component
@Slf4j
public class RestreamerApplicationStartupRunner implements ApplicationRunner {

    @Autowired
    private StreamerCloner streamerCloner;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try {
            streamerCloner.start();
        }
        catch (Exception e) {
            log.error("ERROR: ", e);
            throw e;
        }

    }
}
