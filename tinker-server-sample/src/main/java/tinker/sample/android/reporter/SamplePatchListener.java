/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tinker.sample.android.reporter;

import android.content.Context;

import com.tencent.tinker.lib.listener.DefaultPatchListener;
import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.File;

import tinker.sample.android.patchserver.TinkerServerManager;

/**
 * Created by zhangshaowen on 16/4/30.
 * optional, you can just use DefaultPatchListener
 * we can check whatever you want whether we actually send a patch request
 * such as we can check rom space or apk channel
 */
public class SamplePatchListener extends DefaultPatchListener {
    private static final String TAG = "Tinker.SamplePatchListener";

    public SamplePatchListener(Context context) {
        super(context);
    }

    /**
     * because we use the defaultCheckPatchReceived method
     * the error code define by myself should after {@code ShareConstants.ERROR_RECOVER_INSERVICE
     *
     * @param path
     * @param newPatch
     * @return
     */
    @Override
    public int patchCheck(String path, boolean isUpgrade) {
        File patchFile = new File(path);
        TinkerLog.i(TAG, "receive a patch file: %s, isUpgrade:%b, file size:%d", path, isUpgrade, SharePatchFileUtil.getFileOrDirectorySize(patchFile));
        int returnCode = super.patchCheck(path, isUpgrade);

        //把这个添加到你的PatchListener实现中
        String patchMd5 = SharePatchFileUtil.getMD5(patchFile);
        TinkerServerManager.reportTinkerPatchListenerFail(returnCode, patchMd5);
        return returnCode;
    }
}
