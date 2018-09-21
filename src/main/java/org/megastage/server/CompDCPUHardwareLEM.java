package org.megastage.server;

public class CompDCPUHardwareLEM extends CompDCPUHardware {
    public MessageLEM data;

    public CompDCPUHardwareLEM() {
        super(DCPUManufactorer.NYA_ELEKTRISKA, DCPUHardwareType.LEM, 0x1802);
    }

    @Override
    public void interrupt(CompDCPU dcpu) {
        char a = dcpu.registers[0];
        char b = dcpu.registers[1];

        switch(a) {
            case 0:
                data.videoAddr = b;
                break;
            case 1:
                data.fontAddr = b;
                break;
            case 2:
                data.paletteAddr = b;
                break;
            case 3:
                // borderColor = (dcpu.registers[1] & 0xF);
                break;
            case 4:
                // dump font
                int offs = dcpu.registers[1];
                for (int i = 0; i < LEMUtil.defaultFont.length; i++) {
                    dcpu.ram[(offs + i & 0xFFFF)] = LEMUtil.defaultFont[i];
                }
                dcpu.ticks += 256;
                break;
            case 5:
                // dump palette
                offs = dcpu.registers[1];
                for (int i = 0; i < LEMUtil.defaultPalette.length; i++) {
                    dcpu.ram[(offs + i & 0xFFFF)] = LEMUtil.defaultPalette[i];
                }
                dcpu.ticks += 16;
                break;
        }
    }

    @Override
    public void tick60hz(CompDCPU dcpu) {
    }

    public boolean isDirty() {
        CompDCPU dcpu = (CompDCPU) World.INSTANCE.getComponent(dcpuEID, CompType.DCPU);
        if(dcpu == null) {
            // only happens while waiting CleanupSystem
            return false;
        }

        dirty |= data.videoAddr == 0 ?
                data.video.update(LEMUtil.defaultVideo):
                data.video.update(dcpu.ram, data.videoAddr, 384);

        dirty |= data.fontAddr == 0 ?
                data.font.update(LEMUtil.defaultFont):
                data.font.update(dcpu.ram, data.fontAddr, 256);

        dirty |= data.paletteAddr == 0 ?
                data.palette.update(LEMUtil.defaultPalette):
                data.palette.update(dcpu.ram, data.paletteAddr, 16);

        return dirty;
    }}
