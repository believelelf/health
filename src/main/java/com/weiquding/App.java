package com.weiquding;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 */
public class App {

    public static final String[] URLS = new String[]{
            "http://10.30.95.17:8765/",
            "http://10.30.95.18:8765/",
            "http://10.30.95.2:8765/",
            "http://10.30.95.3:8765/",
            "http://10.30.95.4:8765/",
            "http://10.30.95.5:8765/",
            "http://10.30.95.6:8774/",
            "http://10.30.95.7:8774/",
            "http://10.30.95.8:8774/",
            "http://10.30.95.9:8774/",
            "http://10.30.95.10:8780/",
            "http://10.30.95.11:8780/",
            "http://10.30.95.12:8780/",
            "http://10.30.95.13:8780/",
            "http://10.30.95.6:8772/",
            "http://10.30.95.7:8772/",
            "http://10.30.95.8:8772/",
            "http://10.30.95.9:8772/",
            "http://10.30.95.6:8779/",
            "http://10.30.95.7:8779/",
            "http://10.30.95.8:8779/",
            "http://10.30.95.9:8779/",
            "http://10.30.95.6:8773/",
            "http://10.30.95.7:8773/",
            "http://10.30.95.8:8773/",
            "http://10.30.95.9:8773/",
            "http://10.30.95.10:8776/",
            "http://10.30.95.11:8776/",
            "http://10.30.95.12:8776/",
            "http://10.30.95.13:8776/",
            "http://10.30.95.10:8775/",
            "http://10.30.95.11:8775/",
            "http://10.30.95.12:8775/",
            "http://10.30.95.13:8775/",
            "http://10.30.95.10:8777/",
            "http://10.30.95.11:8777/",
            "http://10.30.95.12:8777/",
            "http://10.30.95.13:8777/",
            "http://10.30.95.18:8783/",
            "http://10.30.95.10:8778/",
            "http://10.30.95.11:8778/",
            "http://10.30.95.12:8778/",
            "http://10.30.95.13:8778/",
            "http://10.30.95.17:9781/",
            "http://10.30.95.6:8771/",
            "http://10.30.95.7:8771/",
            "http://10.30.95.8:8771/",
            "http://10.30.95.9:8771/",
            "http://10.30.95.20:8867/"
    };

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java -jar health-jar-with-dependencies.jar [uptime|heap]");
            return;
        }
        // http://10.30.95.20:8867/instances/bec17a961cea/actuator/metrics/process.uptime
        //{"name":"process.uptime","description":"The uptime of the Java virtual machine","baseUnit":"seconds","measurements":[{"statistic":"VALUE","value":1718527.272}],"availableTags":[]}
        //http://10.30.95.20:8867/instances/bec17a961cea/actuator/metrics/jvm.memory.used?tag=area:heap
        //{"name":"jvm.memory.used","description":"The amount of used memory","baseUnit":"bytes","measurements":[{"statistic":"VALUE","value":9.18147744E8}],"availableTags":[{"tag":"id","values":["Par Survivor Space","CMS Old Gen","Par Eden Space"]}]}
        //http://10.30.95.20:8867/instances/bec17a961cea/actuator/metrics/jvm.memory.max?tag=area:heap
        //{"name":"jvm.memory.max","description":"The maximum amount of memory in bytes that can be used for memory management","baseUnit":"bytes","measurements":[{"statistic":"VALUE","value":2.031484928E9}],"availableTags":[{"tag":"id","values":["Par Survivor Space","CMS Old Gen","Par Eden Space"]}]}
        if ("uptime".equals(args[0])) {
            for (String url : URLS) {
                String uptime = getUpdateTime(getResponse(url + "actuator/metrics/process.uptime"));
                System.out.println(uptime);
            }
        } else if ("heap".equals(args[0])) {
            for (String url : URLS) {
                String heapSize = getHeapSize(getResponse(url + "actuator/metrics/jvm.memory.used?tag=area:heap"), getResponse(url + "actuator/metrics/jvm.memory.max?tag=area:heap"));
                System.out.println(heapSize);
            }
        } else {
            System.out.println("Usage: java -jar health-jar-with-dependencies.jar [uptime|heap]");
        }

    }


    public static String getResponse(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // print result
            return response.toString();
        } else {
            return "";
        }
    }

    public static String getHeapSize(String used, String max) {
        StringBuilder builder = new StringBuilder();
        Map<String, Object> map = JSON.parseObject(used, Map.class);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>) map.get("measurements");
        for (Map<String, Object> measurement : measurements) {
            BigDecimal value = (BigDecimal) measurement.get("value");
            builder.append(parseHeapSize(value.longValue())).append("/");
        }
        map = JSON.parseObject(max, Map.class);
        measurements = (List<Map<String, Object>>) map.get("measurements");
        for (Map<String, Object> measurement : measurements) {
            BigDecimal value = (BigDecimal) measurement.get("value");
            builder.append(parseHeapSize(value.longValue()));
        }
        return builder.toString();
    }

    private static String parseHeapSize(long longValue) {
        double mbValue = (int) (longValue / (1024 * 1024));
        if (mbValue < 1024) {
            return (int) mbValue + " MB";
        }
        return BigDecimal.valueOf(mbValue / 1024).setScale(2, BigDecimal.ROUND_HALF_UP) + " GB";
    }


    public static String getUpdateTime(String uptime) {
        Map<String, Object> map = JSON.parseObject(uptime, Map.class);
        List<Map<String, Object>> measurements = (List<Map<String, Object>>) map.get("measurements");
        for (Map<String, Object> measurement : measurements) {
            BigDecimal value = (BigDecimal) measurement.get("value");
            return parseUpdateTime(value.doubleValue());
        }
        return "";
    }


    public static String parseUpdateTime(double updateTime) {
        StringBuilder builder = new StringBuilder();
        //7d 17h 53m 34s
        int value = (int) updateTime / (24 * 60 * 60);
        updateTime = updateTime % (24 * 60 * 60);
        builder.append(value).append("d ");
        value = (int) updateTime / (60 * 60);
        updateTime = updateTime % (60 * 60);
        builder.append(value).append("h ");
        value = (int) updateTime / (60);
        updateTime = updateTime % (60);
        builder.append(value).append("m ");
        builder.append((int) updateTime).append("s ");
        return builder.toString();
    }


}
