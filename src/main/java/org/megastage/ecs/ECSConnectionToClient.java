package org.megastage.ecs;


import com.esotericsoftware.kryonet.network.ClientConnection;
import org.megastage.ecs.messages.ECSMessageToServer;

public class ECSConnectionToClient extends ClientConnection {
    public String nick = "Unknown";
    public State state = State.WaitingForLogin;

    public int player;
    public int item;
    public ECSMessageToServer receivedMessage;

    public enum State {
        WaitingForLogin, WaitingForInitialData, InGame
    }
}
