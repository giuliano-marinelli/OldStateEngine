package gamelogic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

public class GameView implements Runnable {

    private String sessionId;
    private String gameState;
    private LinkedList<State> states;
    private LinkedList<StaticState> staticStates;
    private HashMap<State, JSONObject> statesSended;
    private HashMap<State, JSONObject> staticStatesSended;
    private Phaser viewsBarrier;
    private boolean playerExit = false;

    GameView(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, Phaser viewsBarrier) {
        this.sessionId = sessionId;
        this.states = states;
        this.staticStates = staticStates;
        this.statesSended = new HashMap<>();
        this.staticStatesSended = new HashMap<>();
        this.viewsBarrier = viewsBarrier;
        this.playerExit = false;
    }

    @Override
    public void run() {
        while (!playerExit) {
            viewsBarrier.arriveAndAwaitAdvance();
            if (!playerExit) {
                createState();
                viewsBarrier.arriveAndAwaitAdvance();
            }
        }
    }

    private void createState() {
        JSONObject jsonStates = new JSONObject();
        JSONObject jsonState;
        int i = 0;
        for (StaticState staticState : staticStates) {
            //generar el estado estatico para la visibilidad del jugador
            jsonState = staticState.toJSON(sessionId, states, staticStates, staticStatesSended.get(staticState));
            if (jsonState != null) {
                staticStatesSended.put(staticState, jsonState);
                jsonStates.put(i + "", jsonState);
                i++;
            }
        }
        for (State state : states) {
            //generar el estado para la visibilidad del jugador
            jsonState = state.toJSON(sessionId, states, staticStates, statesSended.get(state));
            if (jsonState != null) {
                statesSended.put(state, jsonState);
                jsonStates.put(i + "", jsonState);
                i++;
            }
        }
        gameState = !jsonStates.isEmpty() ? jsonStates.toString() : null;
    }

    public String getGameState() {
        return gameState;
    }

    public synchronized void stop() {
        playerExit = true;
        notifyAll();
    }
}
