package org.megastage.server;

import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSEntitySystem;
import org.megastage.ecs.ECSException;
import org.megastage.ecs.ECSWorld;

public class DcpuSystem extends ECSEntitySystem {
    public DcpuSystem(ECSWorld world, long interval, int... components) {
        super(world, interval, components);
    }

    @Override
    protected void processEntity(ECSEntity eid) throws ECSException {

    }
}
