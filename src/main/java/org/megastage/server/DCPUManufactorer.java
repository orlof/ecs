package org.megastage.server;

public enum DCPUManufactorer {
    NYA_ELEKTRISKA(0x1C6C8B36),
    MOJANG(0x4AB55488),
    MACKAPAR(0x1EB37E91),
    GENERAL_DRIVES(0xe1e0bd31),
    TALON_NAVIGATION(0x982d3e46),
    PRECISION_RESEARCH(0x352ad8bf),
    SEIKORION(0xcf115b97),
    OTEC(0xb8badde8),
    ENDER_INNOVATIONS(0xE142A1FA),
    CRADLE_TECH(0xa3783fc8),
    SORATOM(0x80a9ddea),
    URI_OASIS(0x3867ab5f);

    public final int id;

    DCPUManufactorer(int id) {
        this.id = id;
    }

    public int high() {
        return id >> 16 & 0xFFFF;
    }

    public int low() {
        return id & 0xFFFF;
    }
}
