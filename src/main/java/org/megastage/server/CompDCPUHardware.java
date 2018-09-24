package org.megastage.server;

import org.megastage.ecs.components.Component;
import org.megastage.ecs.components.ECSComponent;

@Component
public abstract class CompDCPUHardware implements ECSComponent {
    public final DCPUManufactorer manufactorer;
    public final DCPUHardwareType type;
    public final int revision;

    protected CompDCPUHardware(DCPUManufactorer manufactorer, DCPUHardwareType type, int revision) {
        this.manufactorer = manufactorer;
        this.type = type;
        this.revision = revision;
    }

    public void query(CompDCPU dcpu) {
        dcpu.registers[0] = (char) (this.type.low());
        dcpu.registers[1] = (char) (this.type.high());
        dcpu.registers[2] = (char) (this.revision & 0xFFFF);
        dcpu.registers[3] = (char) (this.manufactorer.low());
        dcpu.registers[4] = (char) (this.manufactorer.high());
    }

    public abstract void interrupt(CompDCPU dcpu);
    public abstract void tick60hz(CompDCPU dcpu);
}
