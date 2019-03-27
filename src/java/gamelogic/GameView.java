package gamelogic;

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
    //private LinkedList<JSONObject> jsonStates;
    private LinkedList<State> states;
    private LinkedList<StaticState> staticStates;
    private Phaser viewsBarrier;
    private boolean playerExit = false;

    GameView(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, Phaser viewsBarrier) {
        this.sessionId = sessionId;
        this.states = states;
        this.staticStates = staticStates;
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
        int i = 0;
        for (State state : states) {
            //generar el estado para la visibilidad del jugador
            jsonStates.put(i + "", state.toJSON(sessionId));
            i++;
        }
        gameState = jsonStates.toString();
    }

    public String getGameState() {
        return gameState;
    }

    public synchronized void stop() {
        playerExit = true;
        notifyAll();
    }
}
