package org.megastage.ecs;

import com.esotericsoftware.kryonet.Connection;

public class ECSConnection extends Connection {
    public String nick = "Unknown";
    public State state = State.WaitingForLogin;

    public int player;
    public int item;

    enum State {
        WaitingForLogin, WaitingForInitialData, InGame
    }
}
