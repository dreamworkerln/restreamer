package ru.kvanttelecom.tv.restreamer.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kvanttelecom.tv.restreamer.configurations.properties.restreamer.RestreamerProperties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

@Component
public class Utils {

    public static final String SEPR = File.separator;

    @Autowired
    RestreamerProperties props;

    public static String getEltexTimeStamp() {

        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int second = now.get(Calendar.SECOND);
        int millis = now.get(Calendar.MILLISECOND);

        return String.format("%d_%02d_%02d_%02d_%02d_%02d_%03d", year, month, day, hour, minute, second, millis);
    }


    /**
     * Replace streamer in masted/media playlists to local one
     * @param playlist
     * @throws MalformedURLException
     */
    public String replaceStreamerToLocal(String playlist) throws MalformedURLException {

        StringBuilder sb = new StringBuilder();
        String[] strings = playlist.split("\n");
        //
        for (String s : strings) {
            if (s.startsWith("#")) {
                sb.append(s).append("\n");
            }
            else {
                URL url = new URL(s);
                int port = url.getPort();
                String address = port > 0 ? url.getHost() + ":" + port : url.getHost();
                sb.append(s.replace(address, props.getAddress())).append("\n");
            }
        }
        return sb.toString();
    }


}
