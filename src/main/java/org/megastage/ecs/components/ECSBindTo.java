package org.megastage.ecs.components;

import org.megastage.ecs.messages.ECSMessage;

@KryoMessage
@AllocateCid
public class ECSBindTo extends ECSMessageComponent {
    private transient int transmittedMaster;
    public int master;

    @Override
    public boolean isDirty() {
        return transmittedMaster != master;
    }

    @Override
    public ECSMessage transmit() {
        transmittedMaster = master;
        return this;
    }
}
