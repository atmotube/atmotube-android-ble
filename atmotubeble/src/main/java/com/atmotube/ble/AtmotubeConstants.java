package com.atmotube.ble;

import java.util.UUID;

public interface AtmotubeConstants {

    public final static UUID UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID UART_RX_CHARACTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID UART_TX_CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    public static final UUID ATMOTUBE_SERVICE_UUID_V2 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab2");

    public static final UUID VOC_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab2");         // 	2 bytes
    public static final UUID HUMIDITY_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab2");    // 	1 byte
    public static final UUID TEMPERATURE_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab2"); // 	1 byte
    public static final UUID STATUS_CHARACTERISTIC_UUID_V2 = UUID.fromString("db450005-8e9a-4818-add7-6ed94a328ab2");      // 	1 byte

    public static final UUID ATMOTUBE_SERVICE_UUID_V3 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab3");
    public static final UUID ATMOTUBE_SERVICE_UUID_V4 = UUID.fromString("db450001-8e9a-4818-add7-6ed94a328ab4");

    public static final UUID VOC_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab3");         // 	2 bytes
    public static final UUID HUMIDITY_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab3");    // 	1 byte
    public static final UUID TEMPERATURE_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab3"); // 	1 byte
    public static final UUID STATUS_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450005-8e9a-4818-add7-6ed94a328ab3");      // 	1 byte
    public static final UUID PRESSURE_CHARACTERISTIC_UUID_V3 = UUID.fromString("db450006-8e9a-4818-add7-6ed94a328ab3");    // 	2 byte

    public static final UUID VOC_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450002-8e9a-4818-add7-6ed94a328ab4");         // 	4 bytes
    public static final UUID BME280_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450003-8e9a-4818-add7-6ed94a328ab4");      // 	6 bytes
    public static final UUID STATUS_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450004-8e9a-4818-add7-6ed94a328ab4");      // 	2 bytes
    public static final UUID PM_CHARACTERISTIC_UUID_V4 = UUID.fromString("db450005-8e9a-4818-add7-6ed94a328ab4");          // 	6 bytes

}
