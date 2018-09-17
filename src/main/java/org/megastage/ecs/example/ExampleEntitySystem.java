package org.megastage.ecs.example;

import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSEntitySystem;
import org.megastage.ecs.ECSException;
import org.megastage.ecs.ECSWorld;

public class ExampleEntitySystem extends ECSEntitySystem {
    public ExampleEntitySystem(ECSWorld world, long interval, int... components) {
        super(world, interval, components);
    }

    @Override
    protected void processEntity(ECSEntity eid) throws ECSException {

    }
}
