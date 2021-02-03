package ru.kvanttelecom.tv.restreamer.services.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;

@Service
@Slf4j
public class AuthServiceStub extends AuthService {

    @Override
    public void authenticate() {
        log.info("AuthService.authenticate() - stub");
    }

    @Override
    public boolean validate(Client client) {
        log.info("AuthService.validate() - stub");
        return true;
    }
}
