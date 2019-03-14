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

/**
 * Data holder with Atmotube packet information
 */
public class UpdateDataHolder implements Parcelable, Serializable {

    public static final int HW_VER_UNKNOWN = 0;
    public static final int HW_VER_1_0 = 1;
    public static final int HW_VER_2_0 = 2;
    public static final int HW_VER_PLUS = 3;
    public static final int HW_VER_PRO = 4;

    public static final int UNKNOWN = -1000;
    public static final int PM_OFF = 0xFFFF;
    public static final float PM_OFF_FLOAT = 167772.15f; // 0xFFFFFF

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
    private String mName;
    private int mHwVer;
    private String mMac;
    private int mRssi;
    private int mErrorCode;
    private int mBatteryVoltage;
    private int mBatteryPercentage = UNKNOWN;

    private float mPm1 = UNKNOWN;
    private float mPm25 = UNKNOWN;
    private float mPm10 = UNKNOWN;

    private int mDeviceCRC = UNKNOWN;

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
        boolean isGeneralDataOK = mVOC != UNKNOWN && mTemperature != UNKNOWN && mHumidity != UNKNOWN && mInfo != null;
        if (isHw3()) {
            return isGeneralDataOK && mPressure != UNKNOWN;
        } else if (isHw4()) {
            return isGeneralDataOK && (!isPmIsOn() || (mPressure != UNKNOWN && mPm1 != UNKNOWN && mPm10 != UNKNOWN && mPm25 != UNKNOWN));
        }
        return isGeneralDataOK;
    }

    public UpdateDataHolder(String name,
                            float voc,
                            float temperature,
                            float humidity,
                            float pressure,
                            Integer info,
                            int adc,
                            String fwVer,
                            String raw,
                            int hwVer,
                            String mac,
                            int rssi,
                            int batteryVoltage,
                            int batteryPercentage,
                            int errorCode) {
        this(name, System.currentTimeMillis() / 1000, voc, temperature, humidity, pressure, info, adc,
                fwVer, raw, hwVer, mac, rssi, batteryVoltage, batteryPercentage, errorCode);
    }

    public UpdateDataHolder(String name,
                            long time,
                            float voc,
                            float temperature,
                            float humidity,
                            float pressure,
                            Integer info,
                            int adc,
                            String fwVer,
                            String raw,
                            int hwVer,
                            String mac,
                            int rssi,
                            int batteryVoltage,
                            int batteryPercentage,
                            int errorCode) {
        mName = name;
        mTime = time;
        mVOC = voc;
        mTemperature = temperature;
        mHumidity = humidity;
        mPressure = pressure;
        mADC = adc;
        mFwVer = fwVer;
        mRaw = raw;
        mHwVer = hwVer;
        mMac = mac;
        mRssi = rssi;
        mBatteryVoltage = batteryVoltage;
        mErrorCode = errorCode;
        setInfo(info);
        if (mInfo != null && mInfo.mHasError) {
            mBatteryVoltage = 0;
            mErrorCode = mBatteryVoltage;
        }
        if (isHw3() || isHw4()) {
            mBatteryPercentage = batteryPercentage;
        }
    }

    public UpdateDataHolder(Parcel in) {
        readFromParcel(in);
    }

    public UpdateDataHolder(float vocF, float temp, int hum, long ts) {
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
        mHwVer = in.readInt();
        mMac = in.readString();
        int b = in.readInt();
        mInfo = b == -1 ? null : new AtmotubeInfo(b, mFwVer);
        mBatteryVoltage = in.readInt();
        mErrorCode = in.readInt();
        mPm1 = in.readFloat();
        mPm25 = in.readFloat();
        mPm10 = in.readFloat();
        mDeviceCRC = in.readInt();
        mBatteryPercentage = in.readInt();
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
        dest.writeInt(mHwVer);
        dest.writeString(mMac);
        dest.writeInt(mInfo == null ? -1 : mInfo.getInfoByte());
        dest.writeInt(mBatteryVoltage);
        dest.writeInt(mErrorCode);
        dest.writeFloat(mPm1);
        dest.writeFloat(mPm25);
        dest.writeFloat(mPm10);
        dest.writeInt(mDeviceCRC);
        dest.writeInt(mBatteryPercentage);
    }

    public static final Creator CREATOR = new Creator() {
        public UpdateDataHolder createFromParcel(Parcel in) {
            return new UpdateDataHolder(in);
        }

        public UpdateDataHolder[] newArray(int size) {
            return new UpdateDataHolder[size];
        }
    };

    public void setInfo(Integer info) {
        if (info != null) {
            mInfo = new AtmotubeInfo(info, mFwVer);
        }
    }

    public void setVOC(float VOC) {
        mVOC = VOC;
        mTime = System.currentTimeMillis() / 1000;
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
        mTime = System.currentTimeMillis() / 1000;
    }

    public void setHumidity(float humidity) {
        mHumidity = humidity;
        mTime = System.currentTimeMillis() / 1000;
    }

    public void updateData(UpdateDataHolder holder) {
        if (holder != null) {
            mTime = System.currentTimeMillis() / 1000;
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

    public void setCurrentFwVersion(String fwVer) {
        mFwVer = fwVer;
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
        if (mBatteryPercentage != UNKNOWN) {
            return mBatteryPercentage;
        }
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

    public Integer getInfoByte() {
        return mInfo != null ? mInfo.getInfoByte() : null;
    }

    public String getMac() {
        return mMac;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public int getBatteryVoltage() {
        return mBatteryVoltage;
    }

    public float getBatteryVoltageFloat() {
        return (float) mBatteryVoltage / 100;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLon() {
        return mLon;
    }

    public void setLon(double lon) {
        mLon = lon;
    }

    public void setPressure(float mPressure) {
        this.mPressure = mPressure;
    }

    public void setTime(long mTime) {
        this.mTime = mTime;
    }

    public void setMac(String mMac) {
        this.mMac = mMac;
    }

    public void setADC(int ADC) {
        mADC = ADC;
    }

    public void setFwVer(String fwVer) {
        mFwVer = fwVer;
    }

    public String getRaw() {
        return mRaw;
    }

    public void setRaw(String raw) {
        mRaw = raw;
    }

    public void setHwVer(int hwVer) {
        mHwVer = hwVer;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    public void setBatteryVoltage(int batteryVoltage) {
        mBatteryVoltage = batteryVoltage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        mBatteryPercentage = batteryPercentage;
    }

    public AtmotubeInfo getInfo() {
        return mInfo;
    }

    public void setInfo(AtmotubeInfo info) {
        mInfo = info;
    }

    public int getHwVer() {
        return mHwVer;
    }

    public boolean isHw2() {
        return mHwVer == HW_VER_2_0;
    }

    public boolean isHw3() {
        return mHwVer == HW_VER_PLUS;
    }

    public boolean isHw4() {
        return mHwVer == HW_VER_PRO;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPm(int pm1, int pm25, int pm10) {
        if (pm1 == PM_OFF || pm25 == PM_OFF || pm10 == PM_OFF) {
            mPm1 = UNKNOWN;
            mPm25 = UNKNOWN;
            mPm10 = UNKNOWN;
            return;
        }
        mPm1 = pm1;
        mPm25 = pm25;
        mPm10 = pm10;
    }

    public void setPm(float pm1, float pm25, float pm10) {
        if (pm1 == PM_OFF_FLOAT || pm25 == PM_OFF_FLOAT || pm10 == PM_OFF_FLOAT) {
            mPm1 = UNKNOWN;
            mPm25 = UNKNOWN;
            mPm10 = UNKNOWN;
            return;
        }
        mPm1 = pm1;
        mPm25 = pm25;
        mPm10 = pm10;
    }

    public float getPm1() {
        return mPm1;
    }

    public float getPm25() {
        return mPm25;
    }

    public float getPm10() {
        return mPm10;
    }

    public String getName() {
        return mName;
    }

    public JSONArray getJSONArray() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(mLat);
        array.put(mLon);
        array.put(AtmotubeUtils.getAQS(mVOC, mPm1, mPm25, mPm10));
        array.put(Math.round(mVOC * 100.0) / 100.0);
        array.put(mTemperature);
        array.put(mHumidity);
        array.put(mTime);
        return array;
    }

    public int getDeviceCRC() {
        return mDeviceCRC;
    }

    public void setDeviceCRC(int deviceCRC) {
        mDeviceCRC = deviceCRC;
    }

    public boolean isBonded() {
        return mInfo != null && mInfo.mIsBonded;
    }

    public boolean isPmIsOn() {
        return mInfo != null && mInfo.mIsPmOn;
    }

}