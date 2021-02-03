package ru.kvanttelecom.tv.restreamer.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import ru.kvanttelecom.tv.restreamer.data.status.ServerStatus;
import ru.kvanttelecom.tv.restreamer.services.MonitorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "/status", produces = {"application/json"})
@Slf4j
public class StatusController {

    @Autowired
    MonitorService monitorService;

    @Autowired
    @Qualifier("mapperWithNull")
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<?> getClientsCount(HttpServletRequest request,
                                                  HttpServletResponse response) {
        ResponseEntity<String> result; // = ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            log.trace("GET: " + request.getRequestURI());
            // save playlist to file
            //String res = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(monitorService.getServerStatus()) + "\n";
            String res = objectMapper.writeValueAsString(monitorService.getServerStatus()) + "\n";
            result = ResponseEntity.ok(res);
            //response.setContentType("application/json");
        }
        catch (Exception e) {
            result = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            log.error("SERVER ERROR:", e);
        }
        log.trace("RESULT: " + result.getStatusCode());
        return result;
    }


}
