package com.atmotube.ble;

import java.util.UUID;

public interface AtmotubeConstants {

    UUID UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    UUID ATMOTUBE_SERVICE_UUID_V2 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab2");

    UUID VOC_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab2");         // 	2 bytes
    UUID HUMIDITY_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab2");    // 	1 byte
    UUID TEMPERATURE_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab2"); // 	1 byte
    UUID STATUS_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450005-8e9a-4818-add7-6ed94a328ab2");      // 	1 byte

    UUID ATMOTUBE_SERVICE_UUID_V3 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab3");
    UUID ATMOTUBE_SERVICE_UUID_V4 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab4");

    UUID VOC_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab3");         // 	4 bytes
    UUID BME280_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab3");      // 	6 bytes
    UUID STATUS_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab3");      // 	2 bytes

    UUID VOC_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab4");         // 	4 bytes
    UUID BME280_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab4");      // 	6 bytes
    UUID STATUS_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab4");      // 	2 bytes
    UUID PM_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450005-8e9a-4818-add7-6ed94a328ab4");          // 	8 bytes

    String DFU_NAME_1_0 = "DfuMode";
    String DFU_NAME_2_0 = "DfuAtmo";
    String DFU_NAME_PLUS = "DfuAtmoPlus";
    String DFU_NAME_PRO = "DfuAtmoPro";

}
