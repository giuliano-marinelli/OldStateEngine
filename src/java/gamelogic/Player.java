package gamelogic;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.json.simple.JSONObject;

public class Player extends Entity {

    protected String id;
    protected int countProjectile;
    protected boolean dead;
    protected boolean leave;
    protected int team;
    protected int role;
    protected int health;
    protected int healthMax;

    public Player(String id, int countProjectile, boolean dead, boolean leave, int team, int role, int health, int healthMax, int x, int y, String name, boolean destroy) {
        super(x, y, name, destroy);
        this.id = id;
        this.countProjectile = countProjectile;
        this.dead = dead;
        this.leave = leave;
        this.team = team;
        this.role = role;
        this.health = health;
        this.healthMax = healthMax;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        Action action = actions.get(id);
        LinkedList<State> newStates = new LinkedList<>();
        if (action != null) {
            if (!dead) {
                switch (action.getName()) {
                    case "fire":
                        int posX = Integer.parseInt(action.getParameter("x"));
                        int posY = Integer.parseInt(action.getParameter("y"));
                        if (posX != x || posY != y) {
                            int xVelocity;
                            int yVelocity;
                            if (posX < x) {
                                xVelocity = -1;
                            } else if (posX > x) {
                                xVelocity = 1;
                            } else {
                                xVelocity = 0;
                            }
                            if (posY < y) {
                                yVelocity = -1;
                            } else if (posY > y) {
                                yVelocity = 1;
                            } else {
                                yVelocity = 0;
                            }
                            Projectile projectile = new Projectile(id, countProjectile, team, xVelocity, yVelocity, x, y, "Projectile", false);
                            newStates.add(projectile);
                        }
                        break;
                }
            }
        }
        Point myFuturePosition = futurePosition(actions);
        for (State state : states) {
            if (state != this && state.getName().equals("Player") && !((Player) state).dead && !((Player) state).leave) {
                Point otherFuturePosition = ((Player) state).futurePosition(actions);
                if (myFuturePosition.x == otherFuturePosition.x && myFuturePosition.y == otherFuturePosition.y) {
                    this.addEvent("collide");
                }
            } else if (state != this && state.getName().equals("Tower")) {
                Tower tower = (Tower) state;
                LinkedList<Point> areaTower = tower.getArea();
                int i = 0;
                boolean collide = false;
                while (i < areaTower.size() && !collide) {
                    if (areaTower.get(i).x == myFuturePosition.x && areaTower.get(i).y == myFuturePosition.y) {
                        collide = true;
                        this.addEvent("collide");
                    }
                    i++;
                }
            }
        }
        return newStates;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        hasChanged = false;
        Action action = actions.get(id);
        int newX = x;
        int newY = y;
        int newCountProjectile = countProjectile;
        boolean newLeave = leave;
        boolean newDead = dead;
        int newRole = role;
        int newHealth = health;
        boolean newDestroy = destroy;
        if (action != null) {
            hasChanged = true;
            //System.out.println("ACTION: " + action.getName());
            if (!dead) {
                switch (action.getName()) {
                    case "up":
                        newY = y - 1;
                        break;
                    case "down":
                        newY = y + 1;
                        break;
                    case "left":
                        newX = x - 1;
                        break;
                    case "right":
                        newX = x + 1;
                        break;
                    case "upleft":
                        newY = y - 1;
                        newX = x - 1;
                        break;
                    case "upright":
                        newY = y - 1;
                        newX = x + 1;
                        break;
                    case "downleft":
                        newY = y + 1;
                        newX = x - 1;
                        break;
                    case "downright":
                        newY = y + 1;
                        newX = x + 1;
                        break;
                    case "fire":
                        newCountProjectile = countProjectile + 1;
                        break;
                }
            }
            switch (action.getName()) {
                case "enter":
                    newLeave = false;
                    break;
                case "leave":
                    newLeave = true;
                    break;
            }
            if (!((Map) staticStates.get(0)).canWalk(new Point(newX, newY))) {
                newX = x;
                newY = y;
            }
        }
        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            hasChanged = true;
            boolean wasRespanwn = false;
            for (String event : events) {
                switch (event) {
                    case "hit":
                        newHealth = health - 10;
                        if (newHealth <= 0) {
                            newDead = true;
                        }
                        System.out.println("Player " + id + " has been killed.");
                        break;
                    case "collide":
                        if (!wasRespanwn) {
                            newX = x;
                            newY = y;
                        }
                        break;
                    case "spawn":
                        System.out.println("Player " + id + " spawn in game.");
                        break;
                    case "respawn":
                        wasRespanwn = true;
                        System.out.println("Player " + id + " respawn in game.");
                        Random random = new Random();
                        LinkedList<Spawn> spawns = new LinkedList<>();
                        if (role == 0) {
                            for (StaticState staticState : staticStates) {
                                if (staticState.name.equals("SpawnDefence")) {
                                    spawns.add((Spawn) staticState);
                                }
                            }
                        } else {
                            for (StaticState staticState : staticStates) {
                                if (staticState.name.equals("SpawnAttack")) {
                                    spawns.add((Spawn) staticState);
                                }
                            }
                        }
                        Spawn spawn = spawns.get(random.nextInt(spawns.size()));
                        newX = spawn.x;
                        newY = spawn.y;
                        newDead = false;
                        newHealth = healthMax;
                        break;
                    case "despawn":
                        newDestroy = true;
                        System.out.println("Player " + id + " despawn of the game.");
                        break;
                }
            }
        }
        Player newPlayer = new Player(id, newCountProjectile, newDead, newLeave, team, newRole, newHealth, healthMax, newX, newY, name, newDestroy);
        return newPlayer;
    }

    public Point futurePosition(HashMap<String, Action> actions) {
        Point position;
        Action action = actions.get(id);
        int newY = y;
        int newX = x;
        if (action != null) {
            switch (action.getName()) {
                case "up":
                    newY = y - 1;
                    break;
                case "down":
                    newY = y + 1;
                    break;
                case "left":
                    newX = x - 1;
                    break;
                case "right":
                    newX = x + 1;
                    break;
                case "upleft":
                    newY = y - 1;
                    newX = x - 1;
                    break;
                case "upright":
                    newY = y - 1;
                    newX = x + 1;
                    break;
                case "downleft":
                    newY = y + 1;
                    newX = x - 1;
                    break;
                case "downright":
                    newY = y + 1;
                    newX = x + 1;
                    break;
            }
        }
        position = new Point(newX, newY);
        return position;
    }

    @Override
    public void setState(State newPlayer) {
        super.setState(newPlayer);
        id = ((Player) newPlayer).id;
        countProjectile = ((Player) newPlayer).countProjectile;
        dead = ((Player) newPlayer).dead;
        leave = ((Player) newPlayer).leave;
        team = ((Player) newPlayer).team;
        role = ((Player) newPlayer).role;
        health = ((Player) newPlayer).health;
        healthMax = ((Player) newPlayer).healthMax;
    }

    @Override
    protected Object clone() {
        Player clon = new Player(id, countProjectile, dead, leave, team, role, health, healthMax, x, y, name, destroy);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonPlayer = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("id", id);
        jsonAttrs.put("countProjectile", countProjectile);
        jsonAttrs.put("dead", dead);
        jsonAttrs.put("leave", leave);
        jsonAttrs.put("team", team);
        jsonAttrs.put("role", role);
        jsonAttrs.put("health", health);
        jsonAttrs.put("healthMax", healthMax);
        jsonPlayer.put("Player", jsonAttrs);
        return jsonPlayer;
    }

}
