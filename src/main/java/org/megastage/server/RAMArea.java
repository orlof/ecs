package org.megastage.server;

import org.megastage.ecs.components.KryoMessage;

import static org.megastage.ecs.ECSUtil.hexlify;

@KryoMessage
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
        return update(mem, 0, mem.length);
    }

    public boolean update(char[] memory, int start, int size) {
        boolean dirty = false;

        for(int i=0; i < size; i++) {
            char c = memory[(start + i) & 0xffff];
            if(c != mem[i]) {
                dirty = true;
                mem[i] = c;
            }
        }

        return dirty;
    }

    public String toString() {
        return hexlify(mem);
    }

}
