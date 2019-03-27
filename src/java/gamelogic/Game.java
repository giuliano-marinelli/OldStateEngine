package gamelogic;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Game implements Runnable {

    private LinkedList<State> states;
    private LinkedList<StaticState> staticStates;
    private HashMap<String, Action> actions;
    private ConcurrentHashMap<String, String> actionsSended; //sessionid -> accion
    private HashMap<String, GameView> gameViews;
    private ConcurrentHashMap<String, String> gameViewsSended; //sessionid -> [enter, leave]
    private Phaser viewsBarrier;
    private String gameState;
    private String gameFullState;
    private String gameStaticState;
    private boolean endGame;

    private Lobby lobby;

    //constructor
    public Game(Lobby lobby) {
        states = new LinkedList<>();
        staticStates = new LinkedList<>();
        actions = new HashMap<>();
        actionsSended = new ConcurrentHashMap();
        gameViews = new HashMap<>();
        gameViewsSended = new ConcurrentHashMap<>();
        viewsBarrier = new Phaser(1);
        endGame = false;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        init();
        createStaticState();
        LinkedList<State> nextStates;
        LinkedList<State> newStates;
        while (!endGame) {
            try {
                Thread.sleep(100); //time per frame (10 fps)
                //readPlayers();
                readActions();
                //se realizan las comunicaciones a traves de eventos y 
                //se generan nuevos estados que seran computados
                newStates = new LinkedList<>();
                for (State state : states) {
                    LinkedList<State> newState = state.generate(states, staticStates, actions);
                    if (newState != null) {
                        newStates.addAll(newState);
                    }
                }
                states.addAll(newStates);
                //se generan los estados siguientes incluyendo los generados
                nextStates = new LinkedList<>();
                for (State state : states) {
                    nextStates.add(state.next(states, staticStates, actions));
                }
                //se crean los nuevos estados con los calculados anteriormente
                for (int i = 0; i < states.size(); i++) {
                    states.get(i).createState(nextStates.get(i));
                    states.get(i).clearEvents();
                }
                createState();
                //recorre los player que entran o salen del juego para agregarlos
                //o quitarlos de la lista de gameViews
                readPlayers();
                //despierta a los hilos para que generen el JSON con el estado
                //correspondiente a la visibilidad de cada jugador
                viewsBarrier.arriveAndAwaitAdvance();
                //barrera hasta que todos los hilos terminan de computar el estado
                viewsBarrier.arriveAndAwaitAdvance();
                lobby.stateReady();
                int i = 0;
                while (i < states.size()) {
                    if (states.get(i).isDestroy()) {
                        //System.out.println("State " + states.get(i).getName() + " is removed.");
                        states.remove(i);
                    } else {
                        i++;
                    }
                }
                //System.out.println("STATIC: " + gameStaticState);
                //System.out.println("DYNAMIC: " + gameFullState);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void init() {
        try {
            //TODO crear estados dinamicos y estaticos
            File map = new File(this.getClass().getClassLoader().getResource("files/map.csv").toURI());
            loadMap(map);
            //match spawnea players cuando todos hicieron ready
            states.add(new Match(1, 2, true, false, false, 0, 4, new LinkedList<String>(),
                    new LinkedList<String>(), new LinkedList<String>(), new LinkedList<Integer>(), "Match", false));
            createSpawns();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadMap(File fileMap) {
        try {
            String linea;
            HashMap<Point, Integer> cells = new HashMap<>();
            BufferedReader buffer = new BufferedReader(new FileReader(fileMap));
            int y = 0;
            int x = 0;
            while ((linea = buffer.readLine()) != null) {
                String[] cols = linea.split(",");
                for (x = 0; x < cols.length; x++) {
                    cells.put(new Point(x, y), Integer.parseInt(cols[x]));
                }
                y++;
            }
            staticStates.add(new gamelogic.Map(cells, x, y, "Map"));

        } catch (IOException ex) {
            Logger.getLogger(Game.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createSpawns() {
        staticStates.add(new Spawn(16, 35, "SpawnAttack"));
        staticStates.add(new Spawn(19, 35, "SpawnAttack"));
        staticStates.add(new Spawn(22, 35, "SpawnAttack"));
        staticStates.add(new Spawn(25, 35, "SpawnAttack"));
        staticStates.add(new Spawn(16, 9, "SpawnDefence"));
        staticStates.add(new Spawn(18, 9, "SpawnDefence"));
        staticStates.add(new Spawn(20, 9, "SpawnDefence"));
        staticStates.add(new Spawn(22, 9, "SpawnDefence"));
        staticStates.add(new Spawn(19, 5, "SpawnTower"));
        staticStates.add(new Spawn(9, 20, "SpawnTower"));
        staticStates.add(new Spawn(29, 20, "SpawnTower"));
    }

    private void createStaticState() {
        JSONObject jsonStaticStates = new JSONObject();
        int i = 0;
        for (StaticState staticState : staticStates) {
            jsonStaticStates.put(i + "", staticState.toJSON());
            i++;
        }
        gameStaticState = jsonStaticStates.toString();
    }

    private void createState() {
        JSONObject jsonFullStates = new JSONObject();
        JSONObject jsonStates = new JSONObject();
        int i = 0;
        int j = 0;
        for (State state : states) {
            jsonFullStates.put(i + "", state.toJSON());
            if (state.hasChanged()) {
                jsonStates.put(j + "", state.toJSON());
                j++;
            }
            i++;
        }
        gameFullState = jsonFullStates.toString();
        gameState = jsonStates.toString();
    }

    public void readActions() {
        actions.clear();
        JSONParser parser = new JSONParser();
        for (Map.Entry<String, String> actionSend : actionsSended.entrySet()) {
            String sessionId = actionSend.getKey();
            String action = actionSend.getValue();
            Action newAction = null;
            try {
                JSONObject jsonAction = (JSONObject) parser.parse(action);
                String actionName = (String) jsonAction.get("name");
                newAction = new Action(sessionId, actionName);

                JSONArray jsonParameters = (JSONArray) jsonAction.get("parameters");
                if (jsonParameters != null) {
                    for (int i = 0; i < jsonParameters.size(); i++) {
                        JSONObject parameter = (JSONObject) jsonParameters.get(i);
                        newAction.putParameter((String) parameter.get("name"), (String) parameter.get("value"));
                    }
                }
            } catch (Exception ex) {
                newAction = new Action(sessionId, action);
            } finally {
                System.out.println("Player " + sessionId + " do action: " + newAction.getName());
                actions.put(sessionId, newAction);
                actionsSended.remove(sessionId);
            }
        }
    }

    public void addAction(String sessionId, String action) {
        //Player player = players.get(sessionId);
        //if (players.containsKey(sessionId)) {
        //    if (!player.isLeave()) {
        //si existe el player y no salio ni murio, entonces puede hacer accion

        if (actionsSended.containsKey(sessionId)) {
            String actualAction = actionsSended.get(sessionId);
            JSONParser parser = new JSONParser();
            int actualPriority = 0;
            try {
                JSONObject jsonAction = (JSONObject) parser.parse(actualAction);
                actualPriority = Integer.parseInt((String) jsonAction.get("priority"));
            } catch (Exception ex) {
                actualPriority = 0;
            }
            int newPriority = 0;
            try {
                JSONObject jsonAction = (JSONObject) parser.parse(action);
                newPriority = Integer.parseInt((String) jsonAction.get("priority"));
            } catch (Exception ex) {
                newPriority = 0;
            }
            if (newPriority > actualPriority) {
                actionsSended.put(sessionId, action);
            }
        } else {
            actionsSended.put(sessionId, action);
        }
        //    }
        //}
    }

    public void readPlayers() {
        if (gameViewsSended.size() > 0) {
            for (Map.Entry<String, String> gameViewSended : gameViewsSended.entrySet()) {
                String sessionId = gameViewSended.getKey();
                String action = gameViewSended.getValue();
                if (action == "enter") {
                    //aumento en uno los miembros de la barrera
                    //(tal ves hay que hacerlo en el hilo del gameView)
                    viewsBarrier.register();
                    //creo el nuevo hilo
                    GameView gameView = new GameView(sessionId, states, staticStates, viewsBarrier);
                    Thread threadGameView = new Thread(gameView);
                    threadGameView.start();
                    //lo agrego a la lista de gridViews
                    gameViews.put(sessionId, gameView);
                } else if (action == "leave") {
                    //disminuyo en uno los miembros de la barrera
                    //(tal ves hay que hacerlo en el hilo del gameView)
                    viewsBarrier.arriveAndDeregister();
                    //mato el hilo seteando su variable de terminancion y realizando un notify
                    gameViews.get(sessionId).stop();
                    //lo elimino de la lista de gridViews
                    gameViews.remove(sessionId);
                }
            }
            gameViewsSended.clear();
        }
    }

    public void addPlayer(String sessionId) {
        gameViewsSended.put(sessionId, "enter");
    }

    public void removePlayer(String sessionId) {
        gameViewsSended.put(sessionId, "leave");
    }

    public boolean isEndGame() {
        return endGame;
    }

    public void endGame() {
        endGame = true;
    }

    public String getGameState() {
        return gameState;
    }

    public String getGameState(String sessionId) {
        return gameViews.get(sessionId).getGameState();
    }

    public String getGameFullState() {
        return gameFullState;
    }

    public String getGameStaticState() {
        return gameStaticState;
    }

}
