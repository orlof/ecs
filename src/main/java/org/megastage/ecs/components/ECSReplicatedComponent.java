package org.megastage.ecs.components;

import org.megastage.ecs.messages.ECSMessageToClient;

public interface ECSReplicatedComponent extends ECSComponent {
    boolean isDirty();
    ECSMessageToClient transmit();
}
