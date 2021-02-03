package ru.kvanttelecom.tv.restreamer.configurations.properties.client;


import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Data
@NoArgsConstructor
public class Client {

    @Value("${client.mac}")
    @Setter(AccessLevel.NONE)
    private String mac;

    // generic "token"
    @Value("${client.token}")
    private String token;

    // Use here JWT or what you need
    @Value("${client.accessToken:}")
    private String accessToken;

    @Value("${client.refreshToken:}")
    private String refreshToken;

    @Setter(AccessLevel.NONE)
    @Autowired
    private User user;

    public String getUid() {
        return user.getUsername() + "-" + mac;
    }



    @Override
    public String toString() {
        return "Client{" +
            "username='" + user.getUsername() + '\'' +
            ", mac='" + mac + '\'' +
            '}';
    }
}
