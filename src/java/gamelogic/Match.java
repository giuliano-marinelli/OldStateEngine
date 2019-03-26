package gamelogic;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Match extends State {

    protected int round;
    protected int countRounds;
    protected boolean endGame;
    protected boolean endRound;
    protected boolean startGame;
    protected int teamAttacker;
    protected int sizeTeam;
    protected LinkedList<String> players;
    protected LinkedList<String> playingPlayers;
    protected LinkedList<String> ready;
    protected LinkedList<Integer> teamPoints;

    public Match(int round, int countRounds, boolean endGame, boolean endRound, boolean startGame, int teamAttacker, int sizeTeam, LinkedList<String> players, LinkedList<String> playingPlayers, LinkedList<String> ready, LinkedList<Integer> teamPoints, String name, boolean destroy) {
        super(name, destroy);
        this.round = round;
        this.countRounds = countRounds;
        this.endGame = endGame;
        this.endRound = endRound;
        this.startGame = startGame;
        this.teamAttacker = teamAttacker;
        this.sizeTeam = sizeTeam;
        this.players = players;
        this.playingPlayers = playingPlayers;
        this.ready = ready;
        this.teamPoints = teamPoints;
    }

    private void reset(LinkedList<State> states) {
        //despawnea a todas las torres, proyectiles y jugadores en el mapa
        for (State state : states) {
            if (state.getName().equals("Tower")) {
                Tower tower = (Tower) state;
                tower.addEvent("despawn");
            } else if (state.getName().equals("Projectile")) {
                Projectile projectile = (Projectile) state;
                projectile.addEvent("collide");
            } else if (state.getName().equals("Player")) {
                Player player = (Player) state;
                player.addEvent("despawn");
            }
        }
    }

    private void spawn(LinkedList<StaticState> staticStates, LinkedList<State> newStates) {
        //obtiene los puntos de spawn para personajes que atacan y defienden, y para las torres
        LinkedList<Spawn> spawnsAttack = new LinkedList<>();
        LinkedList<Spawn> spawnsDefence = new LinkedList<>();
        LinkedList<Spawn> spawnsTower = new LinkedList<>();
        for (StaticState staticState : staticStates) {
            if (staticState.name.equals("SpawnAttack")) {
                spawnsAttack.add((Spawn) staticState);
            } else if (staticState.name.equals("SpawnDefence")) {
                spawnsDefence.add((Spawn) staticState);
            } else if (staticState.name.equals("SpawnTower")) {
                spawnsTower.add((Spawn) staticState);
            }
        }

        //spawnea a los jugadores en las posiciones de spawn
        LinkedList<String> spawnPlayers = playingPlayers;
        if (playingPlayers.isEmpty()) {
            spawnPlayers = ready;
            //mezcla a los jugadores para formar aleatoriamente los equipos
            //en caso de haber mas de sizeTeam*2 jugadores permitira que se elijan jugadores al azar entre los de la sala
            Collections.shuffle(spawnPlayers);
        }
        boolean attack = teamAttacker == 0;
        int i = 0;
        int j = 0;
        int maxCant = 0;
        for (String player : spawnPlayers) {
            if (maxCant < sizeTeam * 2) {
                Player newPlayer;
                if (attack) {
                    newPlayer = new Player(player, 0, false, false, teamAttacker, 1, 100, 100, spawnsAttack.get(i).x, spawnsAttack.get(i).y, "Player", false);
                    newStates.add(newPlayer);
                    newPlayer.addEvent("spawn");
                    i++;
                } else {
                    newPlayer = new Player(player, 0, false, false, 1 - teamAttacker, 0, 100, 100, spawnsDefence.get(j).x, spawnsDefence.get(j).y, "Player", false);
                    newStates.add(newPlayer);
                    newPlayer.addEvent("spawn");
                    j++;
                }
                attack = !attack;
                maxCant++;
            }
        }
        //spawnea a las torres en las posiciones de spawn
        Tower towerMain = new Tower("towerMain", 0, false, 1 - teamAttacker, 400, 400, 3, 3, spawnsTower.get(0).x, spawnsTower.get(0).y, "Tower", false);
        newStates.add(towerMain);
        towerMain.addEvent("spawn");
        Tower towerLeft = new Tower("towerLeft", 0, false, 1 - teamAttacker, 200, 200, 3, 3, spawnsTower.get(1).x, spawnsTower.get(1).y, "Tower", false);
        newStates.add(towerLeft);
        towerLeft.addEvent("spawn");
        Tower towerRight = new Tower("towerRight", 0, false, 1 - teamAttacker, 200, 200, 3, 3, spawnsTower.get(2).x, spawnsTower.get(2).y, "Tower", false);
        newStates.add(towerRight);
        towerRight.addEvent("spawn");
    }

    public boolean teamsReady() {
        boolean teamsReady = false;
        int playerReadys = 0;
        int i = 0;
        while (i < players.size()) {
            playerReadys += ready.contains(players.get(i)) ? 1 : 0;
            i++;
        }
        if (playerReadys >= sizeTeam * 2) {
            teamsReady = true;
        }
        return teamsReady;
    }

    public boolean allReady() {
        boolean allReady = true;
        if (ready.isEmpty()) {
            allReady = false;
        } else {
            int i = 0;
            while (allReady && i < players.size()) {
                allReady = ready.contains(players.get(i));
                i++;
            }
        }
        return allReady;
    }

    public boolean allTowersDestroy(LinkedList<State> states) {
        boolean allDestroy = true;
        int i = 0;
        while (allDestroy && i < states.size()) {
            if (states.get(i).getName().equals("Tower")) {
                Tower tower = (Tower) states.get(i);
                allDestroy = tower.dead;
            }
            i++;
        }
        return allDestroy;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        LinkedList<State> newStates = new LinkedList<>();
        if ((allReady() || teamsReady()) && !startGame) {
            reset(states);
            spawn(staticStates, newStates);
            addEvent("start");
        }
        if (endRound) {
            if (round == countRounds) {
                reset(states);
                addEvent("end");
            } else {
                reset(states);
                spawn(staticStates, newStates);
                addEvent("startround");
            }
        }
        if (playingPlayers.isEmpty() && startGame) {
            reset(states);
        }
        if (allTowersDestroy(states) && startGame && !endRound) {
            addEvent("endround");
        }
        if (startGame) {
            Random random = new Random();
            int countTeam0 = 0;
            int countTeam1 = 0;
            for (State state : states) {
                if (state.getName().equals("Player")) {
                    Player player = (Player) state;
                    if (!player.leave) {
                        if (player.team == 0) {
                            countTeam0++;
                        } else {
                            countTeam1++;
                        }
                        if (player.dead) {
                            if (random.nextInt(100) <= 1) {
                                player.addEvent("respawn");
                            }
                        }
                    }
                }
            }
            if (countTeam0 == 0 || countTeam1 == 0) {
                reset(states);
                addEvent("end");
            }
        }
        for (java.util.Map.Entry<String, Action> actionEntry : actions.entrySet()) {
            String id = actionEntry.getKey();
            Action action = actionEntry.getValue();
            hasChanged = true;
            switch (action.getName()) {
                case "restart":
                    reset(states);
                    addEvent("end");
            }
        }
        return newStates;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        hasChanged = false;
        int newRound = round;
        boolean newStartGame = startGame;
        boolean newEndGame = endGame;
        boolean newEndRound = endRound;
        int newTeamAttacker = teamAttacker;
        LinkedList<String> newPlayers = (LinkedList<String>) players.clone();
        LinkedList<String> newPlayingPlayers = (LinkedList<String>) playingPlayers.clone();
        LinkedList<String> newReady = (LinkedList<String>) ready.clone();
        LinkedList<Integer> newTeamPoints = (LinkedList<Integer>) teamPoints.clone();
        for (java.util.Map.Entry<String, Action> actionEntry : actions.entrySet()) {
            String id = actionEntry.getKey();
            Action action = actionEntry.getValue();
            hasChanged = true;
            switch (action.getName()) {
                case "enter":
                    newPlayers.add(id);
                    break;
                case "ready":
                    if (!ready.contains(id) && !startGame) {
                        newReady.add(id);
                    }
                    break;
                case "leave":
                    newPlayers.remove(id);
                    newPlayingPlayers.remove(id);
                    newReady.remove(id);
                    break;
                case "restart":
                    newPlayers.clear();
                    newPlayingPlayers.clear();
                    newReady.clear();
                    break;
            }
        }
        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            hasChanged = true;
            for (String event : events) {
                switch (event) {
                    case "start":
                        System.out.println("Match started.");
                        newStartGame = true;
                        //newEndGame = false;
                        newRound = 1;
                        //newReady = new LinkedList<>();
                        //permite agregar a playingPlayers unicamente los elegidos aleatoriamente
                        LinkedList<String> playersStates = new LinkedList<>();
                        for (State state : states) {
                            if (state.getName().equals("Player")) {
                                Player player = (Player) state;
                                playersStates.add(player.id);
                                newPlayingPlayers.add(player.id);
                            }
                        }
                        //aca les quita el ready a aquellos que quedaron afuera
                        for (String player : players) {
                            if (!playersStates.contains(player)) {
                                newReady.remove(player);
                            }
                        }

                        //newPlayingPlayers.addAll(players);
                        newTeamPoints = new LinkedList<>();
                        newTeamPoints.add(0);
                        newTeamPoints.add(0);
                        break;
                    case "end":
                        System.out.println("Match ended.");
                        newEndRound = false;
                        newStartGame = false;
                        //newEndGame = true;
                        newReady = new LinkedList<>();
                        newPlayingPlayers = new LinkedList<>();
                        break;
                    case "startround":
                        System.out.println("Round " + (round + 1) + " started.");
                        newEndRound = false;
                        newRound = round + 1;
                        break;
                    case "endround":
                        System.out.println("Round " + round + " ended.");
                        newEndRound = true;
                        newTeamAttacker = 1 - teamAttacker;
                        break;
                }
            }
        }
        if (startGame) {
            hasChanged = true;
            if (teamAttacker == 1) {
                newTeamPoints.set(0, teamPoints.get(0) + 1);
            } else {
                newTeamPoints.set(1, teamPoints.get(1) + 1);
            }
        }
        if (playingPlayers.isEmpty() && startGame) {
            hasChanged = true;
            System.out.println("All players are left, match reset.");
            newTeamPoints = new LinkedList<>();
            newStartGame = false;
            //newEndGame = true;
            newEndRound = false;
            newRound = 1;
        }
        Match newMatch = new Match(newRound, countRounds, newEndGame, newEndRound, newStartGame, newTeamAttacker, sizeTeam, newPlayers, newPlayingPlayers, newReady, newTeamPoints, name, destroy);
        return newMatch;
    }

    @Override
    public void setState(State newMatch) {
        super.setState(newMatch);
        round = ((Match) newMatch).round;
        countRounds = ((Match) newMatch).countRounds;
        endGame = ((Match) newMatch).endGame;
        endRound = ((Match) newMatch).endRound;
        startGame = ((Match) newMatch).startGame;
        teamAttacker = ((Match) newMatch).teamAttacker;
        sizeTeam = ((Match) newMatch).sizeTeam;
        players = ((Match) newMatch).players;
        playingPlayers = ((Match) newMatch).playingPlayers;
        ready = ((Match) newMatch).ready;
        teamPoints = ((Match) newMatch).teamPoints;
    }

    @Override
    protected Object clone() {
        Match clon = new Match(round, countRounds, endGame, endRound, startGame, teamAttacker, sizeTeam, players, playingPlayers, ready, teamPoints, name, destroy);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonMatch = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("round", round);
        jsonAttrs.put("countRounds", countRounds);
        jsonAttrs.put("endGame", endGame);
        jsonAttrs.put("endRound", endRound);
        jsonAttrs.put("startGame", startGame);
        jsonAttrs.put("teamAttacker", teamAttacker);
        jsonAttrs.put("sizeTeam", sizeTeam);

        JSONArray jsonPlayers = new JSONArray();
        for (String player : players) {
            jsonPlayers.add(player);
        }
        jsonAttrs.put("players", jsonPlayers);

        JSONArray jsonPlayingPlayers = new JSONArray();
        for (String playingPlayer : playingPlayers) {
            jsonPlayingPlayers.add(playingPlayer);
        }
        jsonAttrs.put("playingPlayers", jsonPlayingPlayers);

        JSONArray jsonReady = new JSONArray();
        for (String aReady : ready) {
            jsonReady.add(aReady);
        }
        jsonAttrs.put("ready", jsonReady);

        JSONArray jsonTeamPoints = new JSONArray();
        for (Integer teamPoint : teamPoints) {
            jsonTeamPoints.add(teamPoint);
        }
        jsonAttrs.put("teamPoints", jsonTeamPoints);

        jsonMatch.put("Match", jsonAttrs);
        return jsonMatch;
    }

}
