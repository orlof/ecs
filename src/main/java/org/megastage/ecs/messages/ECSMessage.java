package org.megastage.ecs.messages;

import org.megastage.ecs.ECSConnection;
import org.megastage.ecs.ECSWorld;

public interface ECSMessage {
    void receive(ECSWorld world, ECSConnection conn);
}
