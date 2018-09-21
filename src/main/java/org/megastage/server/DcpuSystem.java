package org.megastage.server;

import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSEntitySystem;
import org.megastage.ecs.ECSException;
import org.megastage.ecs.ECSWorld;

public class DcpuSystem extends ECSEntitySystem {
    private static final boolean SHIFT_ROLL_BEHAVIOR = false;

    public DcpuSystem(ECSWorld world, long interval) {
        super(world, interval, CompDCPU.cid);
    }

    @Override
    protected void processEntity(ECSEntity eid) throws ECSException {
        CompDCPU dcpu = (CompDCPU) eid.component[CompDCPU.cid];

        long uptime = time - dcpu.startTime;
        if (uptime > 0) {
            long tickTarget = uptime * dcpu.hz / 1000;

            while (dcpu.ticks < tickTarget) {
                tick(dcpu);

                if (dcpu.ticks > dcpu.nextHardwareTick) {
                    tickHardware(dcpu);
                }
            }
        }
    }

    private void tickHardware(CompDCPU dcpu) {
        dcpu.nextHardwareTick += dcpu.hardwareTickInterval;

        for (int i = 0; i < dcpu.hardwareSize; i++) {
            tick60hz(dcpu, dcpu.hardware[i]);
        }
    }

    private void tick(CompDCPU dcpu) {
        dcpu.ticks++;
        if (dcpu.isOnFire) {
            int pos = (int) (Math.random() * 0x10000) & 0xFFFF;
            char val = (char) ((int) (Math.random() * 0x10000) & 0xFFFF);
            int len = (int) (1 / (Math.random() + 0.001f)) - 0x50;
            for (int i = 0; i < len; i++) {
                dcpu.ram[(pos + i) & 0xFFFF] = val;
            }
        }

        if (dcpu.isSkipping) {
            char opcode = dcpu.ram[dcpu.pc];
            int cmd = opcode & 0x1F;
            dcpu.pc = (char) (dcpu.pc + getInstructionLength(opcode));
            dcpu.isSkipping = (cmd >= 16) && (cmd <= 23);
            return;
        }

        if (!dcpu.queueingEnabled) {
            if (dcpu.ip != dcpu.iwp) {
                char a = dcpu.interrupts[dcpu.ip = dcpu.ip + 1 & 0xFF];
                if (dcpu.ia > 0) {
                    dcpu.queueingEnabled = true;
                    dcpu.ram[--dcpu.sp & 0xFFFF] = dcpu.pc;
                    dcpu.ram[--dcpu.sp & 0xFFFF] = dcpu.registers[0];
                    dcpu.registers[0] = a;
                    dcpu.pc = dcpu.ia;
                }
            }
        }

        char opcode = dcpu.ram[dcpu.pc++];

        int cmd = opcode & 0x1F;
        if (cmd == 0) {
            cmd = opcode >> 5 & 0x1F;
            if (cmd != 0) {
                int atype = opcode >> 10 & 0x3F;
                int aaddr = getAddrA(dcpu, atype);
                char a = get(dcpu, aaddr);

                switch (cmd) {
                    case 1: //JSR
                        dcpu.ticks += 2;
                        dcpu.ram[--dcpu.sp & 0xFFFF] = dcpu.pc;
                        dcpu.pc = a;
                        break;
                    //case 7: //HCF
                    //    dcpu.ticks += 8;
                    //    dcpu.isOnFire = true;
                    //    break;
                    case 8: //INT
                        dcpu.ticks += 3;
                        dcpu.interrupt(a);
                        break;
                    case 9: //IAG
                        set(dcpu, aaddr, dcpu.ia);
                        break;
                    case 10: //IAS
                        dcpu.ia = a;
                        break;
                    case 11: //RFI
                        dcpu.ticks += 2;
                        //disables interrupt queueing, pops A from the stack, then pops PC from the stack
                        dcpu.queueingEnabled = false;
                        dcpu.registers[0] = dcpu.ram[dcpu.sp++ & 0xFFFF];
                        dcpu.pc = dcpu.ram[dcpu.sp++ & 0xFFFF];
                        break;
                    case 12: //IAQ
                        dcpu.ticks++;
                        //if a is nonzero, interrupts will be added to the queue instead of triggered. if a is zero, interrupts will be triggered as normal again
                        dcpu.queueingEnabled = a != 0;
                        break;
                    case 16: //HWN
                        //Log.info("HWN " + hardware.size());
                        dcpu.ticks++;
                        set(dcpu, aaddr, (char) dcpu.hardwareSize);
                        break;
                    case 17: //HWQ
                        dcpu.ticks += 3;
                        if (a < dcpu.hardwareSize) {
                            //Log.info("HWQ " + ((int) a) + " " + hardware.get(a).toString());
                            query(dcpu, dcpu.hardware[a]);
                        }
                        break;
                    case 18: //HWI
                        dcpu.ticks += 3;
                        if (a < dcpu.hardwareSize) {
                            interrupt(dcpu, dcpu.hardware[a]);
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 13:
                    case 14:
                    case 15:
                    default:
                        break;
                }
            }
        } else {
            int atype = opcode >> 10 & 0x3F;

            char a = getValA(dcpu, atype);

            int btype = opcode >> 5 & 0x1F;
            int baddr = getAddrB(dcpu, btype);
            char b = get(dcpu, baddr);

            switch (cmd) {
                case 1: { //SET
                    set(dcpu, baddr, a);
                    return;
                }
                case 2: { //ADD
                    dcpu.ticks++;
                    int val = b + a;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 3: { //SUB
                    dcpu.ticks++;
                    int val = b - a;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 4: { //MUL
                    dcpu.ticks++;
                    int val = b * a;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 5: { //MLI
                    dcpu.ticks++;
                    int val = (short) b * (short) a;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 6: { //DIV
                    dcpu.ticks += 2;
                    if (a == 0) {
                        set(dcpu, baddr, (char) 0);
                        dcpu.ex = 0;
                    } else {
                        set(dcpu, baddr, (char) (b / a));
                        dcpu.ex = (char) (((long) b << 16) / a);
                    }
                    return;
                }
                case 7: { //DVI
                    dcpu.ticks += 2;
                    if (a == 0) {
                        set(dcpu, baddr, (char) 0);
                        dcpu.ex = 0;
                    } else {
                        b = (char) ((short) b / (short) a);
                        dcpu.ex = (char) ((b << 16) / ((short) a));
                    }
                    return;
                }
                case 8: {//MOD
                    dcpu.ticks += 2;
                    if (a == 0) {
                        set(dcpu, baddr, (char) 0);
                    } else {
                        set(dcpu, baddr, (char) (b % a));
                    }
                    return;
                }
                case 9: { //MDI
                    dcpu.ticks += 2;
                    if (a == 0) {
                        set(dcpu, baddr, (char) 0);
                    } else {
                        set(dcpu, baddr, (char) ((short) b % (short) a));
                    }
                    return;
                }
                case 10: { //AND
                    set(dcpu, baddr, (char) (b & a));
                    return;
                }
                case 11: { //BOR
                    set(dcpu, baddr, (char) (b | a));
                    return;
                }
                case 12: { //XOR
                    set(dcpu, baddr, (char) (b ^ a));
                    return;
                }
                case 13: { //SHR
                    if(SHIFT_ROLL_BEHAVIOR && a > 31) {
                        set(dcpu, baddr, (char) 0);
                        dcpu.ex = (char) 0;
                    } else {
                        set(dcpu, baddr, (char) (b >>> a));
                        dcpu.ex = (char) (b << 16 >>> a);
                    }
                    return;
                }
                case 14: { //ASR
                    if(SHIFT_ROLL_BEHAVIOR && a > 31) {
                        a = (char) 31;
                    }
                    set(dcpu, baddr, (char) ((short)b >> a));
                    dcpu.ex = (char) (b << 16 >> a);
                    return;
                }
                case 15: { //SHL
                    if(SHIFT_ROLL_BEHAVIOR && a > 31) {
                        set(dcpu, baddr, (char) 0);
                        dcpu.ex = (char) 0;
                    } else {
                        set(dcpu, baddr, (char) (b << a));
                        dcpu.ex = (char) (b << a >> 16);
                    }
                    return;
                }
                case 16: //IFB
                    dcpu.ticks++;
                    if ((b & a) == 0) dcpu.skip();
                    return;
                case 17: //IFC
                    dcpu.ticks++;
                    if ((b & a) != 0) dcpu.skip();
                    return;
                case 18: //IFE
                    dcpu.ticks++;
                    if (b != a) dcpu.skip();
                    return;
                case 19: //IFN
                    dcpu.ticks++;
                    if (b == a) dcpu.skip();
                    return;
                case 20: //IFG
                    dcpu.ticks++;
                    if (b <= a) dcpu.skip();
                    return;
                case 21: //IFA
                    dcpu.ticks++;
                    if ((short) b <= (short) a) dcpu.skip();
                    return;
                case 22: //IFL
                    dcpu.ticks++;
                    if (b >= a) dcpu.skip();
                    return;
                case 23: //IFU
                    dcpu.ticks++;
                    if ((short) b >= (short) a) dcpu.skip();
                    return;
                case 26: { //ADX
                    dcpu.ticks += 2;
                    int val = b + a + dcpu.ex;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 27: { //SBX
                    dcpu.ticks += 2;
                    int val = b - a + dcpu.ex;
                    set(dcpu, baddr, (char) val);
                    dcpu.ex = (char) (val >> 16);
                    return;
                }
                case 30: //STI
                    dcpu.ticks++;
                    set(dcpu, baddr, a);
                    dcpu.registers[6]++;
                    dcpu.registers[7]++;
                    return;
                case 31: //STD
                    dcpu.ticks++;
                    set(dcpu, baddr, a);
                    dcpu.registers[6]--;
                    dcpu.registers[7]--;
                    return;
                case 24:
                case 25:
            }
        }
    }

    private int getAddrB(CompDCPU dcpu, int type) {
        switch (type & 0xF8) {
            case 0x00:
                return 0x10000 + (type & 0x7);
            case 0x08:
                return dcpu.registers[type & 0x7];
            case 0x10:
                dcpu.ticks++;
                return dcpu.ram[dcpu.pc++] + dcpu.registers[type & 0x7] & 0xFFFF;
            case 0x18:
                switch (type & 0x7) {
                    case 0x0:
                        return (--dcpu.sp) & 0xFFFF;
                    case 0x1:
                        return dcpu.sp & 0xFFFF;
                    case 0x2:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.pc++] + dcpu.sp & 0xFFFF;
                    case 0x3:
                        return 0x10008;
                    case 0x4:
                        return 0x10009;
                    case 0x5:
                        return 0x10010;
                    case 0x6:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.pc++];
                }
                dcpu.ticks++;
                return 0x20000 | dcpu.ram[dcpu.pc++];
        }

        throw new IllegalStateException("Illegal a value type " + Integer.toHexString(type) + "! How did you manage that!?");
    }

    private int getAddrA(CompDCPU dcpu, int type) {
        if (type >= 0x20) {
            return 0x20000 | (type & 0x1F) + 0xFFFF & 0xFFFF;
        }

        switch (type & 0xF8) {
            case 0x00:
                return 0x10000 + (type & 0x7);
            case 0x08:
                return dcpu.registers[type & 0x7];
            case 0x10:
                dcpu.ticks++;
                return dcpu.ram[dcpu.pc++] + dcpu.registers[type & 0x7] & 0xFFFF;
            case 0x18:
                switch (type & 0x7) {
                    case 0x0:
                        return dcpu.sp++ & 0xFFFF;
                    case 0x1:
                        return dcpu.sp & 0xFFFF;
                    case 0x2:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.pc++] + dcpu.sp & 0xFFFF;
                    case 0x3:
                        return 0x10008;
                    case 0x4:
                        return 0x10009;
                    case 0x5:
                        return 0x10010;
                    case 0x6:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.pc++];
                }
                dcpu.ticks++;
                return 0x20000 | dcpu.ram[dcpu.pc++];
        }

        throw new IllegalStateException("Illegal a value type " + Integer.toHexString(type) + "! How did you manage that!?");
    }

    private char getValA(CompDCPU dcpu, int type) {
        if (type >= 0x20) {
            return (char) ((type & 0x1F) + 0xFFFF);
        }

        switch (type & 0xF8) {
            case 0x00:
                return dcpu.registers[type & 0x7];
            case 0x08:
                return dcpu.ram[dcpu.registers[type & 0x7]];
            case 0x10:
                dcpu.ticks++;
                return dcpu.ram[dcpu.ram[dcpu.pc++] + dcpu.registers[type & 0x7] & 0xFFFF];
            case 0x18:
                switch (type & 0x7) {
                    case 0x0:
                        return dcpu.ram[dcpu.sp++ & 0xFFFF];
                    case 0x1:
                        return dcpu.ram[dcpu.sp & 0xFFFF];
                    case 0x2:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.ram[dcpu.pc++] + dcpu.sp & 0xFFFF];
                    case 0x3:
                        return dcpu.sp;
                    case 0x4:
                        return dcpu.pc;
                    case 0x5:
                        return dcpu.ex;
                    case 0x6:
                        dcpu.ticks++;
                        return dcpu.ram[dcpu.ram[dcpu.pc++]];
                }
                dcpu.ticks++;
                return dcpu.ram[dcpu.pc++];
        }

        throw new IllegalStateException("Illegal a value type " + Integer.toHexString(type) + "! How did you manage that!?");
    }

    private char get(CompDCPU dcpu, int addr) {
        if (addr < 0x10000)
            return dcpu.ram[addr & 0xFFFF];
        if (addr < 0x10008)
            return dcpu.registers[addr & 0x7];
        if (addr >= 0x20000)
            return (char) addr;
        if (addr == 0x10008)
            return dcpu.sp;
        if (addr == 0x10009)
            return dcpu.pc;
        if (addr == 0x10010)
            return dcpu.ex;
        throw new IllegalStateException("Illegal address " + Integer.toHexString(addr) + "! How did you manage that!?");
    }

    private void set(CompDCPU dcpu, int addr, char val) {
        if (addr < 0x10000)
            dcpu.ram[addr & 0xFFFF] = val;
        else if (addr < 0x10008) {
            dcpu.registers[addr & 0x7] = val;
        } else if (addr < 0x20000) {
            if (addr == 0x10008)
                dcpu.sp = val;
            else if (addr == 0x10009)
                dcpu.pc = val;
            else if (addr == 0x10010)
                dcpu.ex = val;
            else
                throw new IllegalStateException("Illegal address " + Integer.toHexString(addr) + "! How did you manage that!?");
        }
    }

    private int getInstructionLength(char opcode) {
        int len = 1;
        int cmd = opcode & 0x1F;
        if (cmd == 0) {
            cmd = opcode >> 5 & 0x1F;
            if (cmd > 0) {
                int atype = opcode >> 10 & 0x3F;
                if (((atype & 0xF8) == 16) || (atype == 26) || (atype == 31) || (atype == 30)) len++;
            }
        } else {
            int atype = opcode >> 5 & 0x1F;
            int btype = opcode >> 10 & 0x3F;
            if (((atype & 0xF8) == 16) || (atype == 26) || (atype == 30) || (atype == 31)) len++;
            if (((btype & 0xF8) == 16) || (btype == 26) || (btype == 30) || (btype == 31)) len++;
        }
        return len;
    }

    private void interrupt(CompDCPU dcpu, int eid) {
        CompDCPUHardware hw = (CompDCPUHardware) world.getComponent(eid, CompDCPUHardware.cid);
        hw.interrupt(dcpu);
    }

    private void tick60hz(CompDCPU dcpu, int eid) {
        CompDCPUHardware hw = (CompDCPUHardware) world.getComponent(eid, CompDCPUHardware.cid);
        hw.tick60hz(dcpu);
    }

    private void query(CompDCPU dcpu, int eid) {
        CompDCPUHardware hw = (CompDCPUHardware) world.getComponent(eid, CompDCPUHardware.cid);
        hw.query(dcpu);
    }
}
