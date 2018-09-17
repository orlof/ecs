package org.megastage.ecs;

import com.esotericsoftware.kryonet.Connection;

public class ECSNetworkConnection extends Connection {
    public int player;
    public int item;
    public boolean isInitialized;
    public String nick;
}
