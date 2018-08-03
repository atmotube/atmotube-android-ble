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

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;

public class UpdateDataHolder implements Parcelable, Serializable {

    private static final String HW_VER_DEFAULT = "0000";
    private static final String HW_VER_1_0 = "0100";
    private static final String HW_VER_1_2 = "0102";
    private static final String HW_VER_1_3 = "0103";

    private static final String HW1 = "1.0";
    private static final String HW2 = "2.0";
    private static final String HW3 = "3.0";

    public static final int UNKNOWN = -1000;

    private float mVOC = UNKNOWN;
    private float mTemperature = UNKNOWN;
    private float mHumidity = UNKNOWN;
    private float mPressure = UNKNOWN;

    private long mTime;
    private double mLat = UNKNOWN;
    private double mLon = UNKNOWN;

    private int mADC;
    private String mFwVer;
    private String mRaw;
    private String mHwVer;
    private String mMac;
    private int mRssi;
    private int mErrorCode;
    private float mBatteryVoltage;

    private AtmotubeInfo mInfo;

    public UpdateDataHolder() {
    }

    public boolean isValid() {
        return mVOC != UNKNOWN;
    }

    public boolean isFullPacket() {
        // on some devices packet data may be incomplete
        return !TextUtils.isEmpty(mFwVer);
    }

    public boolean isValidAllData() {
        return mVOC != UNKNOWN && mTemperature != UNKNOWN && mHumidity != UNKNOWN && mInfo != null && mVOC > 0;
    }

    public UpdateDataHolder(float voc, float temperature, float humidity, int info, int adc, String fwVer, String raw, String hw, String mac, int rssi,
                            float batteryVoltage, int errorCode) {
        this(System.currentTimeMillis() / 1000, voc, temperature, humidity, 0, info, adc, fwVer, raw, hw, mac, rssi, batteryVoltage, errorCode);
    }

    public UpdateDataHolder(float voc, float temperature, float humidity, float pressure, int info, String fwVer, String raw, String mac, int rssi, float batteryVoltage, int errorCode) {
        this(System.currentTimeMillis() / 1000, voc, temperature, humidity, pressure, info, 0, fwVer, raw, HW_VER_1_3, mac, rssi, batteryVoltage, errorCode);
    }

    public UpdateDataHolder(long time, float voc, float temperature, float humidity, float pressure, int info,
                            int adc, String fwVer, String raw, String hw, String mac, int rssi, float batteryVoltage, int errorCode) {
        mTime = time;
        mVOC = voc;
        mTemperature = temperature;
        mHumidity = humidity;
        mPressure = pressure;
        mADC = adc;
        mFwVer = fwVer;
        mRaw = raw;
        mHwVer = hw;
        mMac = mac;
        mRssi = rssi;
        mBatteryVoltage = batteryVoltage;
        mErrorCode = errorCode;
        setInfo(info);
    }

    public UpdateDataHolder(Parcel in) {
        readFromParcel(in);
    }

    public UpdateDataHolder(float vocF, int temp, int hum, long ts) {
        mTemperature = temp;
        mHumidity = hum;
        mTime = ts;
        mVOC = vocF;
    }

    public void readFromParcel(Parcel in) {
        mVOC = in.readFloat();
        mTemperature = in.readFloat();
        mHumidity = in.readFloat();
        mPressure = in.readFloat();
        mTime = in.readLong();
        mLat = in.readDouble();
        mLon = in.readDouble();
        mADC = in.readInt();
        mFwVer = in.readString();
        mRaw = in.readString();
        mHwVer = in.readString();
        mMac = in.readString();
        mInfo = new AtmotubeInfo(in.readInt(), mFwVer);
        mBatteryVoltage = in.readFloat();
        mErrorCode = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(mVOC);
        dest.writeFloat(mTemperature);
        dest.writeFloat(mHumidity);
        dest.writeFloat(mPressure);
        dest.writeLong(mTime);
        dest.writeDouble(mLat);
        dest.writeDouble(mLon);
        dest.writeInt(mADC);
        dest.writeString(mFwVer);
        dest.writeString(mRaw);
        dest.writeString(mHwVer);
        dest.writeString(mMac);
        dest.writeInt(mInfo.getInfoByte());
        dest.writeFloat(mBatteryVoltage);
        dest.writeInt(mErrorCode);
    }

    public static final Creator CREATOR = new Creator() {
        public UpdateDataHolder createFromParcel(Parcel in) {
            return new UpdateDataHolder(in);
        }

        public UpdateDataHolder[] newArray(int size) {
            return new UpdateDataHolder[size];
        }
    };

    public void setInfo(int info) {
        mInfo = new AtmotubeInfo(info, mFwVer);
        mTime = System.currentTimeMillis();
    }

    public void setVOC(float VOC) {
        mVOC = VOC;
        mTime = System.currentTimeMillis();
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
        mTime = System.currentTimeMillis();
    }

    public void setHumidity(float humidity) {
        mHumidity = humidity;
        mTime = System.currentTimeMillis();
    }

    public void updateData(UpdateDataHolder holder) {
        if (holder != null) {
            mTime = System.currentTimeMillis();
            if (holder.mVOC != UNKNOWN) {
                mVOC = holder.mVOC;
            }
            if (holder.mHumidity != UNKNOWN) {
                mHumidity = holder.mHumidity;
            }
            if (holder.mTemperature != UNKNOWN) {
                mTemperature = holder.mTemperature;
            }
            mLat = holder.mLat;
            mLon = holder.mLon;
            if (holder.mInfo != null) {
                mInfo = holder.mInfo;
            }
        }
    }

    public String getFwVer() {
        return mFwVer != null ? mFwVer.toUpperCase() : null;
    }

    public String getHwVer() {
        if (TextUtils.equals(mHwVer, UpdateDataHolder.HW_VER_DEFAULT) || TextUtils.equals(mHwVer, UpdateDataHolder.HW_VER_1_0)) {
            return HW1;
        } else if (TextUtils.equals(mHwVer, UpdateDataHolder.HW_VER_1_2)) {
            return HW2;
        } else if (TextUtils.equals(mHwVer, UpdateDataHolder.HW_VER_1_3)) {
            return HW2;
        }
        if (mFwVer != null) {
            if (mFwVer.startsWith("70")) {
                return HW1;
            } else if (mFwVer.startsWith("72")) {
                return HW2;
            } else if (mFwVer.startsWith("73")) {
                return HW3;
            }
        }
        return "0.0";
    }

    public void setCurrentFwVersion(String fwVer) {
        mFwVer = fwVer;
    }

    public boolean isHw2() {
        return TextUtils.equals(getHwVer(), HW2);
    }

    public boolean isHw3() {
        return TextUtils.equals(getHwVer(), HW3);
    }

    public boolean isActivated() {
        return mInfo != null && mInfo.mIsActivated;
    }

    public boolean hasFwVer() {
        return TextUtils.isEmpty(mFwVer);
    }

    public long getTime() {
        return mTime;
    }

    public boolean isCharging() {
        return mInfo != null && mInfo.mIsCharging;
    }

    public boolean isChargingTimeout() {
        return mInfo != null && mInfo.mIsChargingTimeout;
    }

    public boolean isCalibrating() {
        return mInfo != null && mInfo.mIsCalibrating;
    }

    public int getBattery() {
        return mInfo != null ? mInfo.mBattery : 0;
    }

    public float getVOC() {
        return mVOC;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public float getPressure() {
        return mPressure;
    }

    public int getADC() {
        return mADC;
    }

    public int getInfoByte() {
        return mInfo != null ? mInfo.getInfoByte() : 0;
    }

    public String getMac() {
        return mMac;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public float getBatteryVoltage() {
        return mBatteryVoltage;
    }

    public JSONArray getJSONArray() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(mLat);
        array.put(mLon);
        array.put(AtmotubeUtils.getAQS(mVOC));
        array.put(Math.round(mVOC * 100.0) / 100.0);
        array.put(mTemperature);
        array.put(mHumidity);
        array.put(mTime);
        return array;
    }
}