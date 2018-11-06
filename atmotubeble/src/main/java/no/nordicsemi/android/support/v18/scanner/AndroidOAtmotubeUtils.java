/*
 * Copyright 2018 NotAnotherOne Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nordicsemi.android.support.v18.scanner;

import android.annotation.TargetApi;
import android.os.Build;

import com.atmotube.ble.AtmotubeUtils;
import com.atmotube.ble.UpdateDataHolder;

public class AndroidOAtmotubeUtils {

    @TargetApi(Build.VERSION_CODES.O)
    public static UpdateDataHolder getDataFromScanResult(android.bluetooth.le.ScanResult data) {
        final int eventType = (data.getDataStatus() << 5)
                | (data.isLegacy() ? 0x10 : 0)
                | (data.isConnectable() ? 0x01 : 0);
        byte[] dataR = data.getScanRecord() != null ? data.getScanRecord().getBytes() : null;
        return AtmotubeUtils.getDataFromScanResult(new ScanResult(data.getDevice(), eventType, data.getPrimaryPhy(), data.getSecondaryPhy(),
                data.getAdvertisingSid(), data.getTxPower(), data.getRssi(), data.getPeriodicAdvertisingInterval(),
                ScanRecord.parseFromBytes(dataR), data.getTimestampNanos()));
    }

}
