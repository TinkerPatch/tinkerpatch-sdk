/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Shengjie Sim Sun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.tencent.tinker.server.client;

import java.io.File;

public interface PatchRequestCallback {
    /**
     * 在请求补丁前,我们可以在这个接口拦截请求
     *
     * @return 返回false, 即不会请求服务器
     */
    boolean beforePatchRequest();

    /**
     * 服务器有新的补丁,并且已经成功的下载
     *
     * @param file           下载好的补丁地址,存放在/data/data/app_name/tinker_server/
     * @param newVersion     新的补丁版本
     * @param currentVersion 当前的补丁版本
     */
    boolean onPatchUpgrade(File file, Integer newVersion, Integer currentVersion);

    /**
     * 向服务器请求新补丁时,下载补丁失败
     *
     * @param e              错误类型
     * @param newVersion     下载失败的新补丁版本
     * @param currentVersion 当前的补丁版本
     */
    void onPatchDownloadFail(Exception e, Integer newVersion, Integer currentVersion);

    /**
     * 与服务器同步时失败
     *
     * @param e 失败类型
     */
    void onPatchSyncFail(Exception e);

    /**
     * 收到服务器清除补丁的请求,如何清除本地补丁,可以自行判断
     */
    void onPatchRollback();

    /**
     * 若使用条件下发方式发布补丁,某些动态改变的条件,可以在这个接口更新。例如是否为wifi
     */
    void updatePatchConditions();
}
