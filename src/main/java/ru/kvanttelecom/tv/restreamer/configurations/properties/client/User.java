package ru.kvanttelecom.tv.restreamer.configurations.properties.client;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Data
@NoArgsConstructor
public class User {

    @Value("${client.user.username}")
    @Setter(AccessLevel.NONE)
    private String username;

    @Value("${client.user.password}")
    @Setter(AccessLevel.NONE)
    private String password;

}
