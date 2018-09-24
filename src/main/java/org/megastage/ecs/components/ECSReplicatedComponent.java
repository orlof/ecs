package org.megastage.ecs.components;

import org.megastage.ecs.messages.ECSMessage;

public interface ECSReplicatedComponent extends ECSComponent {
    boolean isDirty();
    ECSMessage transmit();
}
