package com.xmonster.tkclient;

/**
 * Created by sun on 27/09/2016.
 */

public interface TKClientFactory {

    /**
     * 每次启动或唤醒调请求 http://{Host}/{appKey}/{appVersion}?d={deviceId}&v={timestamp}
     *
     * @return the patch version
     */
    String sync();

    /**
     * 若本地未下载过这个补丁版本，则请求
     * http://{Host}/{appKey}/{appVersion}/file{patchVersion}?d={deviceId}&v={timestamp}
     *
     * @param patchVersion     the patch version which return by sync api
     * @param downloadFilePath the path which update package is stored
     * @return is success
     */
    boolean download(String patchVersion, String downloadFilePath);

}
