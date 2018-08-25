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

import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * Utility class for parsing Atmotube BLE packet data
 * <p>
 * There are several generations of Atmotube devices:
 * - Atmotube 1.0 (Indiegogo edition) - CCS801 VOC sensor, constant heating mode; SHT20: temperature, humidity
 * - Atmotube 2.0 - CCS801 VOC sensor, pulse heating mode (10 seconds); SHT20: temperature, humidity
 * - Atmotube Plus (3.0) - SGPC3 VOC sensor, pulse heating mode (2 seconds); BME280: temperature, humidity, pressure
 * - Atmotube Pro - SGPC3 VOC sensor, pulse heating mode (2 seconds); BME280: temperature, humidity, pressure; PM1/PM2.5/PM10 sensor
 */
public class AtmotubeUtils {

    // Atmotube general name
    private static final String ATMOTUBE_NAME = "atmotube";
    // Atmotube Plus/Pro factory mode
    private static final String ATMOTEST_V_3_0_NAME = "atmotest3";

    // Atmotube v.1.0 identifiers
    private static final byte[] ATMOTUBE_ID_VER_1_0_V1 = {0, 0, 0, 0, 0, 0x41, 0x54, 0x4d, 0x4f, 0x54, 0x75, 0x55, 0x42};
    private static final byte[] ATMOTUBE_ID_VER_1_0_V2 = {0, 0, 0x41, 0x54, 0x4d, 0x4f, 0x54, 0x45, 0x53, 0x54};
    private static final byte[] ATMOTUBE_ID_VER_1_0_V3 = {0, 0, 0, 0, 0, (byte) 0x9e, (byte) 0xca, (byte) 0xdc, 0x24, 0x0e, (byte) 0xe5, (byte) 0xa9, (byte) 0xe0, (byte) 0x93, (byte) 0xf3, (byte) 0xa3, (byte) 0xb5, 0x01, 0x00, 0x40, 0x6e};

    // Atmotube v.2.0 identifiers
    private static final byte[] ATMOTUBE_ID_VER_2_0_V1 = {0, 0, 0, 0, 0, (byte) 0x79, (byte) 0xb7, (byte) 0xa5, (byte) 0xaf, (byte) 0xfe, (byte) 0xee, (byte) 0xf6, (byte) 0xbf, (byte) 0x11, (byte) 0x42, (byte) 0xaa, (byte) 0xbd, 0x01, 0x00, (byte) 0x89, (byte) 0xd8};
    private static final byte[] ATMOTUBE_ID_VER_2_0_V2 = {0, 0, 0, 0, 0, (byte) 0xb2, (byte) 0x8a, (byte) 0x32, (byte) 0x4a, (byte) 0xd9, (byte) 0x6e, (byte) 0xd7, (byte) 0xad, (byte) 0x18, (byte) 0x48, (byte) 0x9a, (byte) 0x8e, 0x01, 0x00, (byte) 0x45, (byte) 0xdb};

    // Factory test mode identifier for Atmotube 1.0 and Atmotube 2.0
    private static final byte[] ATMOTEST = {0x41, 0x54, 0x4d, 0x4f, 0x54, 0x45, 0x53, 0x54};

    // Atmotube packet versions
    private static final int ATMOTUBE_UNKNOWN = 0;
    private static final int ATMOTUBE_PACKET_V1 = 1;
    private static final int ATMOTUBE_PACKET_V2 = 2;
    private static final int ATMOTUBE_PACKET_V3 = 3;

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Return Air Quality Score (AQS) for ppm value
     *
     * @param voc VOC ppm
     * @return AQS 0 to 100
     */
    public static int getAQS(float voc) {
        if (voc < 0.5) {
            return (int) (100 - 60 * voc);
        } else if (voc < 2) {
            return (int) ((118 - 26 * voc) / 1.5f);
        } else {
            int aqs = (int) ((374 - 44 * voc) / 6.5f);
            return aqs < 0 ? 0 : aqs;
        }
    }

    /**
     * Check if current record is Atmotube in factory mode
     *
     * @param scanRecord scan record bytes
     * @return true if scan record is from Atmotube in factory mode
     */
    private static boolean isAtmotest(byte[] scanRecord) {
        int index = 0;
        for (byte b : scanRecord) {
            if (b == ATMOTEST[index]) {
                index++;
                if (ATMOTEST.length == index) {
                    return true;
                }
            } else {
                index = 0;
            }
        }
        return false;
    }

    /**
     * Detect Atmotube packet version for v.1.0 and v.2.0
     *
     * @param scanRecord scan record bytes
     * @return packet version {@link #ATMOTUBE_PACKET_V1}, {@link #ATMOTUBE_PACKET_V2} or {@link #ATMOTUBE_PACKET_V3}
     */
    private static int getPacketVersion(byte[] scanRecord) {
        if (scanRecord == null) {
            return ATMOTUBE_UNKNOWN;
        }
        int type = ATMOTUBE_PACKET_V1;
        for (int i = 5; i < ATMOTUBE_ID_VER_1_0_V1.length; i++) {
            if (ATMOTUBE_ID_VER_1_0_V1[i] != scanRecord[i]) {
                type = ATMOTUBE_UNKNOWN;
                break;
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_PACKET_V2;
            for (int i = 2; i < ATMOTUBE_ID_VER_1_0_V2.length; i++) {
                if (ATMOTUBE_ID_VER_1_0_V2[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_PACKET_V3;
            for (int i = 5; i < ATMOTUBE_ID_VER_1_0_V3.length; i++) {
                if (ATMOTUBE_ID_VER_1_0_V3[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_PACKET_V3;
            for (int i = 5; i < ATMOTUBE_ID_VER_2_0_V1.length; i++) {
                if (ATMOTUBE_ID_VER_2_0_V1[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_PACKET_V3;
            for (int i = 5; i < ATMOTUBE_ID_VER_2_0_V2.length; i++) {
                if (ATMOTUBE_ID_VER_2_0_V2[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        return type;
    }

    /**
     * Parse Atmotube v1 and v2 packet data
     *
     * @param mac        Atmotube MAC address
     * @param scanRecord raw scan record bytes
     * @param rssi       rssi level
     * @return {@link UpdateDataHolder} or null if scanRecord does not contain valid Atmotube data
     */
    private static UpdateDataHolder getDataFromBytes(String mac, byte[] scanRecord, int rssi) {
        int type = getPacketVersion(scanRecord);
        if (type == ATMOTUBE_UNKNOWN) {
            // not possible
            return null;
        }
        int shift = type == ATMOTUBE_PACKET_V1 ? ATMOTUBE_ID_VER_1_0_V1.length : type == ATMOTUBE_PACKET_V2 ? ATMOTUBE_ID_VER_1_0_V2.length : ATMOTUBE_ID_VER_1_0_V3.length;
        String vocStr = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 4]}) + AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 5]});
        int voc = 0;
        try {
            voc = Integer.parseInt(vocStr, 16);
        } catch (Exception ignore) {
            // safeguard
        }
        float vocF = (float) voc / 100;
        int hum = getHumidity(scanRecord[shift + 6]);
        int temp = getTemperature(scanRecord[shift + 7]);
        int info = (int) scanRecord[shift + 8];
        int shift2 = type == ATMOTUBE_PACKET_V1 || type == ATMOTUBE_PACKET_V2 ? shift : shift + 14;
        char char1 = (char) (scanRecord[shift2 + 15] & 0xFF);
        char char2 = (char) (scanRecord[shift2 + 16] & 0xFF);
        String fwVer = new String(new char[]{char1, char2});
        try {
            if (scanRecord[shift2 + 17] > 0) {
                fwVer = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift2 + 17], scanRecord[shift2 + 18], scanRecord[shift2 + 19]});
            }
        } catch (Exception ignore) {
            // safeguard
        }
        int adc = 0;
        String hw = null;
        boolean isActivated = true;
        if (isAtmotest(scanRecord)) {
            isActivated = false;
            try {
                fwVer = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift2 + 9], scanRecord[shift2 + 10], scanRecord[shift2 + 11]});
                hw = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 12]}) + AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 13]});
            } catch (Exception ignore) {
                // safeguard
            }
        }
        if (!isActivated) {
            adc = voc;
        } else {
            try {
                String adcStr = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 34]}) + AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 35]});
                adc = Integer.parseInt(adcStr, 16);
                hw = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 36]}) + AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 37]});
            } catch (Exception ignore) {
                // safeguard
            }
        }
        return new UpdateDataHolder(vocF, temp, hum, 0, info, adc, fwVer, AtmotubeUtils.toHexString(scanRecord), getHardwareVer(fwVer, hw), mac, rssi, 0, 0);
    }

    private static int getHardwareVer(String fwVer, String hwVer) {
        if (TextUtils.equals(hwVer, "0000") || TextUtils.equals(hwVer, "0100")) {
            return UpdateDataHolder.HW_VER_1_0;
        } else if (TextUtils.equals(hwVer, "0102")) {
            return UpdateDataHolder.HW_VER_2_0;
        } else if (TextUtils.equals(hwVer, "0103")) {
            return UpdateDataHolder.HW_VER_2_0;
        }
        if (fwVer != null) {
            if (fwVer.startsWith("70")) {
                return UpdateDataHolder.HW_VER_1_0;
            } else if (fwVer.startsWith("72")) {
                return UpdateDataHolder.HW_VER_2_0;
            } else if (fwVer.startsWith("73")) {
                return UpdateDataHolder.HW_VER_PLUS;
            } else if (fwVer.startsWith("74")) {
                return UpdateDataHolder.HW_VER_PRO;
            }
        }
        return UpdateDataHolder.HW_VER_UNKNOWN;
    }

    /**
     * Parse Atmotube packet data
     *
     * @param data {@link ScanResult} data
     * @return {@link UpdateDataHolder} or null if scanRecord does not contain valid Atmotube data
     */
    public static UpdateDataHolder getDataFromScanResult(ScanResult data) {
        if (data == null || data.getScanRecord() == null || data.getDevice() == null || data.getDevice().getName() == null) {
            return null;
        }
        float vocF = 0;
        int info = 0;
        float vF = 0;
        float temp = 0;
        float pressure = 0;
        int hum = 0;
        String fwVer = "";
        if (TextUtils.equals(data.getDevice().getName().toLowerCase(), ATMOTEST_V_3_0_NAME)) {
            ScanRecord scanRecord = data.getScanRecord();
            SparseArray<byte[]> mData = scanRecord.getManufacturerSpecificData();
            int errorCode = 0;
            if (mData != null && mData.size() == 1) {
                byte[] bytes = mData.get(mData.keyAt(0));
                int shift = 0;
                String vocStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                int voc = Integer.parseInt(vocStr, 16);
                vocF = (float) voc / 1000;
                shift++; // skip battery
                info = 0x20;
                String voltageStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                int voltage = Integer.parseInt(voltageStr, 16);
                vF = (float) voltage / 100;
                fwVer = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                errorCode = (int) bytes[shift];
            }
            return new UpdateDataHolder(vocF, temp, hum, pressure, info, 0, fwVer, AtmotubeUtils.toHexString(scanRecord.getBytes()),
                    UpdateDataHolder.HW_VER_PLUS, data.getDevice().getAddress(), data.getRssi(), vF, errorCode);
        } else if (TextUtils.equals(data.getDevice().getName().toLowerCase(), ATMOTUBE_NAME)) {
            ScanRecord scanRecord = data.getScanRecord();
            List<ParcelUuid> services = scanRecord.getServiceUuids();
            if (services != null) {
                for (ParcelUuid service : services) {
                    String uuid = service.getUuid().toString();
                    if (TextUtils.equals(uuid, AtmotubeConstants.ATMOTUBE_SERVICE_UUID_V3.toString()) ||
                            TextUtils.equals(uuid, AtmotubeConstants.ATMOTUBE_SERVICE_UUID_V4.toString())) {
                        // atmotube v3
                        byte[] bytes = data.getScanRecord().getBytes();
                        int shift = 7;
                        String vocStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                        int voc = 0;
                        try {
                            voc = Integer.parseInt(vocStr, 16);
                        } catch (Exception ignore) {
                        }
                        vocF = (float) voc / 1000;
                        String voltageStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                        int voltage = 0;
                        try {
                            voltage = Integer.parseInt(voltageStr, 16);
                        } catch (Exception ignore) {
                            // safeguard - damaged
                        }
                        vF = (float) voltage / 100;
                        hum = getHumidity(bytes[shift++]);
                        temp = getTemperature(bytes[shift++]);
                        String pressureStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) +
                                AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                        int p = Integer.parseInt(pressureStr, 16);
                        pressure = (float) p / 100;
                        info = (int) bytes[shift];
                        if (TextUtils.equals(uuid, AtmotubeConstants.ATMOTUBE_SERVICE_UUID_V4.toString())) {
                            shift = 51;
                            String pm1Str = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                            int pm1 = Integer.parseInt(pm1Str, 16);
                            String pm25Str = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                            int pm25 = Integer.parseInt(pm25Str, 16);
                            String pm10Str = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                            int pm10 = Integer.parseInt(pm10Str, 16);
                            fwVer = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift]});
                            UpdateDataHolder holder = new UpdateDataHolder(vocF, temp, hum, pressure, info, 0, fwVer, AtmotubeUtils.toHexString(scanRecord.getBytes()),
                                    UpdateDataHolder.HW_VER_PLUS, data.getDevice().getAddress(), data.getRssi(), vF, 0);
                            holder.setPm(pm1, pm25, pm10);
                            return holder;
                        } else {
                            shift = 57;
                            fwVer = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift]});
                            return new UpdateDataHolder(vocF, temp, hum, pressure, info, 0, fwVer, AtmotubeUtils.toHexString(scanRecord.getBytes()),
                                    UpdateDataHolder.HW_VER_PLUS, data.getDevice().getAddress(), data.getRssi(), vF, 0);
                        }
                    }
                }
            }
        }
        // atmotube v1 or v2
        return getDataFromBytes(data.getDevice().getAddress(), data.getScanRecord().getBytes(), data.getRssi());
    }

    private static int getHumidity(byte b) {
        return (int) b & 0xFF;
    }

    private static int getTemperature(byte b) {
        return b;
    }

    /**
     * Prints bytes array to hex string
     *
     * @param b bytes array
     * @return hex string
     */
    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte aB : b) {
            // look up high nibble char
            sb.append(HEX_CHARS[(aB & 0xf0) >>> 4]);
            // look up low nibble char
            sb.append(HEX_CHARS[aB & 0x0f]);
        }
        return sb.toString();
    }
}
