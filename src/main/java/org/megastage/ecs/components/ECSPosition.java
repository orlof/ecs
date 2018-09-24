package org.megastage.ecs.components;

import com.jme3.math.Vector3f;
import org.megastage.ecs.ECSConnection;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.messages.ECSMessage;

@AllocateCid
public class ECSPosition extends ECSMessageComponent {
    private final transient Vector3f transmittedValue = new Vector3f();

    public Vector3f value;

    @Override
    public boolean isDirty() {
        return (transmittedValue.x != value.x)
                || (transmittedValue.y != value.y)
                || (transmittedValue.z != value.z);
    }

    @Override
    public ECSMessage transmit() {
        transmittedValue.x = value.x;
        transmittedValue.y = value.y;
        transmittedValue.z = value.z;

        return this;
    }

    @Override
    public void receive(ECSWorld world, ECSConnection conn) {
        world.setComponent(eid, this);
    }
}
