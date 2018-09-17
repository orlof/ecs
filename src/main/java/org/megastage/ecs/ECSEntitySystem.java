package org.megastage.ecs;

public abstract class ECSEntitySystem extends ECSSystem {
    protected ECSEntityGroup group;

    public ECSEntitySystem(ECSWorld world, long interval, int... components) {
        super(world, interval);

        group = world.createGroup(components);
    }

    protected void processSystem() {
        for(ECSEntity entity: group) {
            processEntity(entity);
        }
    }

    protected abstract void processEntity(ECSEntity eid) throws ECSException;
}
