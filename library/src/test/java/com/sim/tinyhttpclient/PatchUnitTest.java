package com.sim.tinyhttpclient;

import com.sim.tinyhttpclient.impl.UrlConnectionHttpclient;

import org.junit.Test;

import java.util.HashMap;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class PatchUnitTest {
    private final String pathUrl = "https://api.github.com?callback=foo";

    @Test
    public void simplePatch() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        client.Patch(pathUrl, null);
    }

    @Test
    public void simplePatchHttps() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        client.Patch(pathUrl, null);
    }

    @Test
    public void simplePatchWithParameter() throws Exception {
        UrlConnectionHttpclient client = new UrlConnectionHttpclient.Builder().build();
        HashMap<String, String> map = new HashMap<>();
        map.put("limit", "1");
        client.Patch("https://api.xmonster.cn/v2/feeds", map);
    }
}