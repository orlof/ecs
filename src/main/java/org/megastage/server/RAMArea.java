package org.megastage.server;

import java.util.Arrays;

public class RAMArea {
    public char[] mem;

    public RAMArea() {}

    public RAMArea(char[] memory, char start, int size) {
        update(memory, start, size);
    }

    public RAMArea(char[] mem) {
        this.mem = mem;
    }

    public boolean update(char[] mem) {
        return update(mem, (char) 0, mem.length);
    }

    public boolean update(char[] memory, char start, int size) {
        if(equals(memory, start, size)) return false;

        this.mem = Arrays.copyOfRange(memory, start, start + size);
        return true;
    }

    public boolean equals(char[] memory, char start, int size) {
        if(size != mem.length) return false;

        for(int i=0; i < size; i++) {
            if(mem[i] != memory[i + start]) return false;
        }

        return true;
    }

    public String toString() {
        return getHex(mem);
    }

    static final String HEXES = "0123456789ABCDEF";
    public static String getHex( char[] raw ) {
        final StringBuilder hex = new StringBuilder( 4 * raw.length + 1);
        for (char c : raw ) {
            hex.append(HEXES.charAt((c & 0xF000) >> 12));
            hex.append(HEXES.charAt((c & 0x0F00) >> 8));
            hex.append(HEXES.charAt((c & 0x00F0) >> 4));
            hex.append(HEXES.charAt((c & 0x000F)));
            hex.append(" ");
        }
        return hex.toString();
    }
}
