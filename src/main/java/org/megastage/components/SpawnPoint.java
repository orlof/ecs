package org.megastage.components;

import org.megastage.ecs.components.AllocateCid;
import org.megastage.ecs.components.ECSComponent;

@AllocateCid
public class SpawnPoint implements ECSComponent {
    public boolean allocated;
}
