package ru.kvanttelecom.tv.restreamer.configurations.properties.client;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
public class ClientPropertiesConfiguration {


    @Configuration
    @Profile("default")
    @PropertySources({
        @PropertySource(value = "classpath:client.properties"),
        @PropertySource(value = "file:client.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:config/client.properties", ignoreResourceNotFound = true),
    })
    static class DefaultProperties {}


    @Configuration
    @Profile("!default")
    @PropertySources({
        @PropertySource(value = "classpath:client-${spring.profiles.active}.properties"),
        @PropertySource(value = "file:client-${spring.profiles.active}.properties", ignoreResourceNotFound = true),
        @PropertySource(value = "file:config/client-${spring.profiles.active}", ignoreResourceNotFound = true),
    })
    static class NonDefaultProperties {}
}