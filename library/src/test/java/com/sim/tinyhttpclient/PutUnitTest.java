package com.sim.tinyhttpclient;

import com.sim.tinyhttpclient.impl.UrlConnectionHttpclient;

import org.junit.Test;

import java.util.HashMap;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PutUnitTest {
    private final String pathUrl = "https://api.github.com?callback=foo";

    @Test
    public void simplePut() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        client.Put(pathUrl, null);
    }

    @Test
    public void simplePutHttps() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        client.Put(pathUrl, null);
    }

    @Test
    public void simplePutWithParameter() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        HashMap<String, String> map = new HashMap<>();
        map.put("limit", "1");
        client.Put("https://api.xmonster.cn/v2/feeds", map);
    }
}