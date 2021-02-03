package ru.kvanttelecom.tv.restreamer.configurations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;
import ru.kvanttelecom.tv.restreamer.configurations.properties.client.Client;
import ru.kvanttelecom.tv.restreamer.services.RestClient;

import java.time.Duration;

@Configuration
public class SpringBeanConfigurations {

    @Autowired
    ApplicationContext applicationContext;

    private final int HTTP_TIMEOUT = 1000;

    @Primary
    @Bean
    public RestTemplate restTemplateNoException(RestTemplateBuilder builder) {
        return
            builder.setConnectTimeout(Duration.ofMillis(HTTP_TIMEOUT))
                .setReadTimeout(Duration.ofMillis(HTTP_TIMEOUT))
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }
    

    @Bean("restTemplateWithException")
    public RestTemplate restTemplateWithException(RestTemplateBuilder builder) {
        return
            builder.setConnectTimeout(Duration.ofMillis(HTTP_TIMEOUT))
                .setReadTimeout(Duration.ofMillis(HTTP_TIMEOUT))
                .build();
    }



    @Bean
    @Primary
    public RestClient restClient(RestTemplate restTemplate, RestreamerProperties props, Client client) {

        return new RestClient(restTemplate, props, client);
    }

    @Bean("restClientWithException")
    public RestClient restClientWithException(@Qualifier("restTemplateWithException") RestTemplate restTemplate,
                                              RestreamerProperties props,
                                              Client client) {

        return new RestClient(restTemplate, props, client);
    }







    @Primary
    @Bean
    public ObjectMapper objectMapper() {

        // ObjectMapper is threadsafe

        // allow convertation to/from Instant
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // will write as string ISO 8601
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        return mapper;
    }


    @Bean("mapperWithNull")
    public ObjectMapper objectMapperIncludeNull() {

        // ObjectMapper is threadsafe

        // allow convertation to/from Instant
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        // will write as string ISO 8601
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        //mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        //mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        return mapper;
    }

}
