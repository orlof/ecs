package org.megastage.ecs.components;

import org.megastage.ecs.ECSConnection;
import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSUtil;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.messages.ECSMessage;

import java.util.Map;

public abstract class ECSMessageComponent implements ECSMessage, ECSReplicatedComponent {
    public int eid;

    @Override
    public void config(int eid, Map<String, String> params, Map<String, ECSEntity> entityMap) {
        this.eid = eid;
    }

    @Override
    public abstract boolean isDirty();

    @Override
    public ECSMessage transmit() {
        return this;
    }

    @Override
    public void receive(ECSWorld world, ECSConnection connection) {
        world.setComponent(eid, this);
    }

    public String toString() {
        return ECSUtil.toString(this);
    }
}
