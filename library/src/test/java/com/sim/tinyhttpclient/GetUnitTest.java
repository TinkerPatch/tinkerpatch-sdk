package com.sim.tinyhttpclient;

import com.sim.tinyhttpclient.impl.UrlConnectionHttpclient;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class GetUnitTest {
    private final String pathUrl = "https://api.xmonster.cn/v2/feeds";

    @Test
    public void simpleGet() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        String response = client.Get(pathUrl, null);
        System.out.print(response);
    }

    @Test
    public void simpleGetHttps() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        String response = client.Get(pathUrl, null);
        System.out.print(response);
    }

    @Test
    public void simpleGetWithParameter() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        HashMap<String, String> map = new HashMap<>();
        map.put("limit", "1");
        String response = client.Get(pathUrl, map);
        System.out.print(response);
    }

    @Test
    public void simpleGetFile() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        HashMap<String, String> map = new HashMap<>();
        map.put("limit", "1");
        File response = client.GetFile(pathUrl, map, "./test.tmp");
        System.out.print(response);
    }
}