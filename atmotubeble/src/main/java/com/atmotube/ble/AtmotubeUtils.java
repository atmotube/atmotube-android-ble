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

public class AtmotubeUtils {

    private static final byte[] ATMOTUBE_ID_VER1 = {0, 0, 0, 0, 0, 0x41, 0x54, 0x4d, 0x4f, 0x54, 0x75, 0x55, 0x42};
    private static final byte[] ATMOTUBE_ID_VER2 = {0, 0, 0, 0, 0, (byte) 0x9e, (byte) 0xca, (byte) 0xdc, 0x24, 0x0e, (byte) 0xe5, (byte) 0xa9, (byte) 0xe0, (byte) 0x93, (byte) 0xf3, (byte) 0xa3, (byte) 0xb5, 0x01, 0x00, 0x40, 0x6e};
    private static final byte[] ATMOTUBE_ID_VER2_1 = {0, 0, 0, 0, 0, (byte) 0x79, (byte) 0xb7, (byte) 0xa5, (byte) 0xaf, (byte) 0xfe, (byte) 0xee, (byte) 0xf6, (byte) 0xbf, (byte) 0x11, (byte) 0x42, (byte) 0xaa, (byte) 0xbd, 0x01, 0x00, (byte) 0x89, (byte) 0xd8};
    private static final byte[] ATMOTUBE_ID_VER2_2 = {0, 0, 0, 0, 0, (byte) 0xb2, (byte) 0x8a, (byte) 0x32, (byte) 0x4a, (byte) 0xd9, (byte) 0x6e, (byte) 0xd7, (byte) 0xad, (byte) 0x18, (byte) 0x48, (byte) 0x9a, (byte) 0x8e, 0x01, 0x00, (byte) 0x45, (byte) 0xdb};

    private static final byte[] ATMOTUBE_ID_VER3 = {0, 0, 0x41, 0x54, 0x4d, 0x4f, 0x54, 0x45, 0x53, 0x54};

    private static final byte[] ATMOTEST = {0x41, 0x54, 0x4d, 0x4f, 0x54, 0x45, 0x53, 0x54};

    private static final int ATMOTUBE_UNKNOWN = 0;
    private static final int ATMOTUBE_VER1 = 1;
    private static final int ATMOTUBE_VER2 = 2;
    private static final int ATMOTUBE_VER3 = 3;

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

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

    public static boolean isValidPacket(byte[] scanRecord) {
        return getPacketVersion(scanRecord) != ATMOTUBE_UNKNOWN;
    }

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

    private static int getPacketVersion(byte[] scanRecord) {
        int type = ATMOTUBE_VER1;
        for (int i = 5; i < ATMOTUBE_ID_VER1.length; i++) {
            if (ATMOTUBE_ID_VER1[i] != scanRecord[i]) {
                type = ATMOTUBE_UNKNOWN;
                break;
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_VER3;
            for (int i = 5; i < ATMOTUBE_ID_VER3.length; i++) {
                if (ATMOTUBE_ID_VER3[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_VER2;
            for (int i = 5; i < ATMOTUBE_ID_VER2.length; i++) {
                if (ATMOTUBE_ID_VER2[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_VER2;
            for (int i = 5; i < ATMOTUBE_ID_VER2_1.length; i++) {
                if (ATMOTUBE_ID_VER2_1[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        if (type == ATMOTUBE_UNKNOWN) {
            type = ATMOTUBE_VER2;
            for (int i = 5; i < ATMOTUBE_ID_VER2_2.length; i++) {
                if (ATMOTUBE_ID_VER2_2[i] != scanRecord[i]) {
                    type = ATMOTUBE_UNKNOWN;
                    break;
                }
            }
        }
        return type;
    }

    public static UpdateDataHolder getDataFromBytes(String mac, byte[] scanRecord, int rssi) {
        int type = getPacketVersion(scanRecord);
        if (type == ATMOTUBE_UNKNOWN) {
            // not possible
            type = ATMOTUBE_VER1;
        }
        int shift = type == ATMOTUBE_VER1 ? ATMOTUBE_ID_VER1.length : type == ATMOTUBE_VER3 ? ATMOTUBE_ID_VER3.length : ATMOTUBE_ID_VER2.length;
        String vocStr = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 4]}) + AtmotubeUtils.toHexString(new byte[]{scanRecord[shift + 5]});
        int voc = Integer.parseInt(vocStr, 16);
        float vocF = (float) voc / 100;
        int hum = getHumidity(scanRecord[shift + 6]);
        int temp = getTemperature(scanRecord[shift + 7]);
        int info = (int) scanRecord[shift + 8];
        int shift2 = type == ATMOTUBE_VER1 || type == ATMOTUBE_VER3 ? shift : shift + 14;
        char char1 = (char) (scanRecord[shift2 + 15] & 0xFF);
        char char2 = (char) (scanRecord[shift2 + 16] & 0xFF);
        String fwVer = new String(new char[]{char1, char2});
        try {
            if (scanRecord[shift2 + 17] > 0) {
                fwVer = AtmotubeUtils.toHexString(new byte[]{scanRecord[shift2 + 17], scanRecord[shift2 + 18], scanRecord[shift2 + 19]});
            }
        } catch (Exception ignore) {
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
            }
        }
        return new UpdateDataHolder(vocF, temp, hum, info, adc, fwVer, AtmotubeUtils.toHexString(scanRecord), hw, mac, rssi, 0, 0);
    }

    public static UpdateDataHolder getDataFromScanResult(ScanResult data) {
        if (data == null || data.getScanRecord() == null || data.getDevice() == null) {
            return new UpdateDataHolder();
        }
        float vocF = 0;
        int info = 0;
        float vF = 0;
        float temp = 0;
        float pressure = 0;
        int hum = 0;
        String fwVer = "";
        if (TextUtils.equals(data.getDevice().getName().toLowerCase(), "atmotest3")) {
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
            return new UpdateDataHolder(vocF, temp, hum, pressure, info, fwVer, AtmotubeUtils.toHexString(scanRecord.getBytes()),
                    data.getDevice().getAddress(), data.getRssi(), vF, errorCode);
        } else if (TextUtils.equals(data.getDevice().getName().toLowerCase(), "atmotube")) {
            ScanRecord scanRecord = data.getScanRecord();
            List<ParcelUuid> services = scanRecord.getServiceUuids();
            for (ParcelUuid service : services) {
                if (TextUtils.equals(service.getUuid().toString(), AtmotubeConstants.ATMOTUBE_SERVICE_UUID_V3.toString())) {
                    // v3
                    byte[] bytes = data.getScanRecord().getBytes();
                    int shift = 7;
                    String vocStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                    int voc = Integer.parseInt(vocStr, 16);
                    vocF = (float) voc / 1000;
                    String voltageStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                    int voltage = Integer.parseInt(voltageStr, 16);
                    vF = (float) voltage / 100;
                    hum = getHumidity(bytes[shift++]);
                    temp = getTemperature(bytes[shift++]);
                    String pressureStr = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) +
                            AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]});
                    int p = Integer.parseInt(pressureStr, 16);
                    pressure = (float) p / 100;
                    info = (int) bytes[shift];
                    shift = 57;
                    fwVer = AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift++]}) + AtmotubeUtils.toHexString(new byte[]{bytes[shift]});
                    return new UpdateDataHolder(vocF, temp, hum, pressure, info, fwVer, AtmotubeUtils.toHexString(scanRecord.getBytes()),
                            data.getDevice().getAddress(), data.getRssi(), vF, 0);
                }
            }
        }
        return getDataFromBytes(data.getDevice().getAddress(), data.getScanRecord().getBytes(), data.getRssi());
    }

    private static int getHumidity(byte b) {
        return (int) b & 0xFF;
    }

    private static int getTemperature(byte b) {
        return b;
    }

    public static String toHexString(byte[] b) {
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
