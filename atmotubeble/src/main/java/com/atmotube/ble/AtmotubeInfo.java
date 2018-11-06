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

package com.atmotube.ble;

import android.text.TextUtils;

public class AtmotubeInfo {

    public static final int MODE_V10_CONSTANT = 0;
    public static final int MODE_V20_10SECONDS = 0;

    public static final int MODE_PLUS_2SECONDS = 0;
    public static final int MODE_PLUS_30SECONDS = 1;

    public static final int MODE_PRO_2SECONDS = 0;
    public static final int MODE_PRO_30SECONDS = 1;

    public final boolean mIsCalibrating;
    public final int mBattery;
    public final int mMode;
    public final boolean mIsChargingTimeout;
    public final boolean mIsCharging;
    public final boolean mIsActivated;
    public final boolean mHasError;
    public final boolean mIsBonded;

    private int mInfoByte;

    public AtmotubeInfo(int info, String fwVer) {
        mInfoByte = info;
        if (TextUtils.equals(fwVer, "700305")) {
            mIsCalibrating = false;
        } else {
            mIsCalibrating = AtmotubeInfo.isCalibrating(info);
        }
        if (fwVer != null && (fwVer.startsWith("73") || fwVer.startsWith("74"))) {
            mHasError = hasError(info);
            mIsBonded = isBonded(info);
            mBattery = 0;
        } else {
            mHasError = false;
            mIsBonded = false;
            mBattery = AtmotubeInfo.getBatteryLevel(info);
        }
        mMode = AtmotubeInfo.getMode(info);
        mIsChargingTimeout = AtmotubeInfo.isChargingTimeout(info);
        mIsCharging = AtmotubeInfo.isCharging(info);
        mIsActivated = AtmotubeInfo.isActivated(info);
    }

    public int getInfoByte() {
        return mInfoByte;
    }

    public static boolean isBonded(int info) {
        return !((info & 0x2) == 0x2);
    }

    public static boolean hasError(int info) {
        return !((info & 0x4) == 0x4);
    }

    public static boolean isCalibrating(int info) {
        return !((info & 0x40) == 0x40);
    }

    public static boolean isCharging(int info) {
        return (info & 0x8) == 0x8;
    }

    public static boolean isChargingTimeout(int info) {
        return (info & 0x10) == 0x10;
    }

    public static int getMode(int info) {
        return (info & 0x80) == 0x80 ? 1 : 0;
    }

    public static boolean isActivated(int info) {
        return !((info & 0x20) == 0x20);
    }

    public static int getBatteryLevel(int info) {
        int batteryLevel = info & 0x7; // atmotube returns 0 to 4
        batteryLevel++;
        return batteryLevel;
    }
}
