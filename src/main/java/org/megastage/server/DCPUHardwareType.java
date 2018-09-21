package org.megastage.server;

public enum DCPUHardwareType {
    LEM(0x7349F615),
    RADAR(0x3442980F),
    FLOPPY(0x4fd524c5),
    KEYBOARD(0x30CF7406),
    CLOCK(0x12D0B402),
    ENGINE(0xa8fb6730),
    PPS(0x3c7742c2),
    //    public static final int TYPE_PPS(0x0cb7cb4c),
    GYRO(0xeec6c4de),
    GRAVITATION_SENSOR(0x3846bc64),
    THERMAL_LASER(0xEEFA0000),
    FORCE_FIELD(0xF1E7D666),
    POWER_PLANT(0x1574886a),
    POWER_CONTROLLER(0xaff14367),
    BATTERY(0x83fc39b2);

    public final int id;

    DCPUHardwareType(int id) {
        this.id = id;
    }

    public int high() {
        return id >> 16 & 0xFFFF;
    }

    public int low() {
        return id & 0xFFFF;
    }
}
