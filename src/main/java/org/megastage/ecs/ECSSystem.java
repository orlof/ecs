package org.megastage.ecs;

public abstract class ECSSystem {
    private long interval;

    protected ECSWorld world;

    protected long time;
    protected double delta;

    protected ECSSystem(ECSWorld world, long interval) {
        this.world = world;
        this.interval = interval;
    }

    protected void initialize() {}

    protected boolean checkProcessing(long gameTime) {
        if(gameTime >= time + interval) {
            delta = (gameTime - time) / 1000.0;
            time = gameTime;

            return true;
        }
        return false;
    }

    protected void process(long gameTime) {
        if(checkProcessing(gameTime)) {
            begin();
            processSystem();
            end();
        }
    }

    protected void begin() {}
    protected void end() {}
    protected void processSystem() {}
}
