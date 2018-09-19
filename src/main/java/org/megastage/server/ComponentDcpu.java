package org.megastage.server;

import org.jdom2.Element;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.components.Component;
import org.megastage.ecs.components.ECSComponent;

import java.io.IOException;
import java.util.Arrays;

@Component
public class ComponentDcpu extends ECSComponent {
    private int hz = 100000;
    private int hardwareTickInterval = hz / 60;

    public boolean powerOn;
    public String rom;

    public int[] hardware = new int[100];
    public int hardwareSize = 0;

    final public char[] ram = new char[65536];
    final public char[] registers = new char[8];

    public char pc;
    public char sp;
    public char ex;
    public char ia;

    public long powerOnTime;
    public long startupTime;
    public long nextHardwareTick;
    public long ticks;

    public boolean isSkipping = false;
    public boolean isOnFire = false;
    public boolean queueingEnabled = false; //TODO: Verify implementation
    public char[] interrupts = new char[256];
    public int ip;
    public int iwp;

    public void config(Element config) {
        rom = config.getAttributeValue("bootrom");
    }

    public void interrupt(char a) {
        interrupts[iwp = iwp + 1 & 0xFF] = a;
        if (iwp == ip) {
            isOnFire = true;
        }
    }

    public void skip() {
        isSkipping = true;
    }

    public void powerOn(char[] romData, long cTime) {
        System.arraycopy(romData, 0, ram, 0, romData.length);

        powerOnTime = cTime + 2500;
        startupTime = powerOnTime + 2500;

        Arrays.fill(registers, (char) 0);
        Arrays.fill(interrupts, (char) 0);

        pc = sp = ex = ia = 0;
        isSkipping = isOnFire = queueingEnabled = false;
        ip = iwp = 0;
        ticks = 0;
        nextHardwareTick = hardwareTickInterval;
    }

    public void addHardware(int eid) {
        hardware[hardwareSize++] = eid;
    }

    public DCPUHardware getHardware(char b) {
        if (b < hardwareSize) {
            return (DCPUHardware) World.INSTANCE.getComponent(hardware[b], CompType.DCPUHardware);
        }
        return null;
    }
}
