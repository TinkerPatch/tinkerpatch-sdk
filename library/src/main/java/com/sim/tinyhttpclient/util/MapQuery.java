package com.sim.tinyhttpclient.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MapQuery {

    public static String urlEncode(String s, String charset) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, charset);
    }

    public static String urlEncode(Map<?,?> map, String charset) throws UnsupportedEncodingException {
        if (map == null) return "";

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                urlEncode(entry.getKey().toString(), charset),
                urlEncode(entry.getValue().toString(), charset)
            ));
        }
        return sb.toString();       
    }

    // TODO: test case
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("p1", 12);
        map.put("p2", "cat");
        map.put("p3", "a & b");
        try {
            System.out.println(urlEncode(map, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // prints "p1=12&p2=cat&p3=a+%26+b"
    }
}