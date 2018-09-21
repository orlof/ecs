package org.megastage.server;

public class CompDCPUHardwareLEM extends CompDCPUHardware {
    public char videoAddr = 0x8000;
    public RAMArea video = new RAMArea(LEMUtil.defaultVideo);

    public char fontAddr = 0x0000;
    public RAMArea font = new RAMArea(LEMUtil.defaultFont);

    public char paletteAddr = 0x0000;
    public RAMArea palette = new RAMArea(LEMUtil.defaultPalette);

    public CompDCPUHardwareLEM() {
        super(DCPUManufactorer.NYA_ELEKTRISKA, DCPUHardwareType.LEM, 0x1802);
    }

    @Override
    public void interrupt(CompDCPU dcpu) {
        char a = dcpu.registers[0];
        char b = dcpu.registers[1];

        switch(a) {
            case 0:
                videoAddr = b;
                break;
            case 1:
                fontAddr = b;
                break;
            case 2:
                paletteAddr = b;
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

    public boolean isDirty(CompDCPU dcpu) {
        boolean dirty = videoAddr == 0 ?
                video.update(LEMUtil.defaultVideo):
                video.update(dcpu.ram, videoAddr, 384);

        dirty |= fontAddr == 0 ?
                font.update(LEMUtil.defaultFont):
                font.update(dcpu.ram, fontAddr, 256);

        dirty |= paletteAddr == 0 ?
                palette.update(LEMUtil.defaultPalette):
                palette.update(dcpu.ram, paletteAddr, 16);

        return dirty;
    }
}
