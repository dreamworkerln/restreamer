package ru.kvanttelecom.tv.restreamer.data.status;

import lombok.Data;

@Data
public class ServerStatus {
    private Integer clients;

    private OtherMetrics otherMetrics = new OtherMetrics();

    @Data
    public static class OtherMetrics {
        public String engine = "турбохренорезка";
    }
}
