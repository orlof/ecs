package org.megastage.ecs;

import com.esotericsoftware.kryonet.Connection;
import org.megastage.ecs.messages.ECSMessage;

public class ECSConnection extends Connection {
    public State state = State.WaitingForLogin;
    public ECSEntity player;
    public ECSMessage received;

    public int item;

    enum State {
        WaitingForLogin, WaitingForInitialData, InGame
    }
}
