package ru.kvanttelecom.tv.restreamer.services.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;
import ru.kvanttelecom.tv.restreamer.services.RestClient;

@Service
public abstract class AuthService {

    @Autowired
    protected RestClient restClient;
    @Autowired
    protected RestreamerProperties props;


    /**
     * Retrieve here JWT
     */
    // headers.setBasicAuth(user.getUsername(), user.getPassword());
    public abstract void authenticate();


    /**
     * Check/update here OAUTH2 JWT tokens
     * If no tokens/outdated use authenticate to get new one
     */
    public abstract boolean validate(Client client);

}
