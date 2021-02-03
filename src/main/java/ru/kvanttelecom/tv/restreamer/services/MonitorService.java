package ru.kvanttelecom.tv.restreamer.services;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.kvanttelecom.tv.restreamer.data.status.ServerStatus;

import javax.annotation.PostConstruct;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MonitorService {

    @Autowired
    private ChunkService chunkService;

    private final DescriptiveStatistics stats = new DescriptiveStatistics();

    private final ServerStatus status = new ServerStatus();

    @PostConstruct
    private void postConstruct() {
        stats.setWindowSize(4);
    }


    public ServerStatus getServerStatus() {
        ServerStatus result = new ServerStatus();
        BeanUtils.copyProperties(status, result);
        return result;
    }


    // ===============================================================
    // 4 чанка, длительностью 6 сек каждый

    // clients count calculation every 24 sec
    @Scheduled(fixedRate = 24000, initialDelay = 24000)
    private void calculateRequestSpeed24() {
        calculateRequestSpeed(4);
    }


    // -------------------------------------------------

    private void calculateRequestSpeed(double ratio) {
        double clientsCountTmp = chunkService.pollChunkRequests() / ratio;
        stats.addValue(clientsCountTmp);

        status.setClients((int) Math.round(stats.getMean()));
        //log.info("CLIENTS (CHUNKS METRICS): {}  SFM4: {}", Math.round(clientsCountTmp), Math.round(status.getClients()));
    }



}
