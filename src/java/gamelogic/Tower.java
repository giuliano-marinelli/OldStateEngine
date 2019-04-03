package gamelogic;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;
import org.json.simple.JSONObject;

public class Tower extends Entity {

    protected String towerId;
    protected int countProjectile;
    protected boolean dead;
    protected int team;
    protected int health;
    protected int healthMax;
    protected int width;
    protected int height;

    public Tower(String towerId, int countProjectile, boolean dead, int team, int health, int healthMax, int width, int height,
            int x, int y,
            String name, boolean destroy, String id) {
        super(x, y, name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.towerId = towerId;
        this.countProjectile = countProjectile;
        this.dead = dead;
        this.team = team;
        this.health = health;
        this.healthMax = healthMax;
        this.width = width;
        this.height = height;
    }

    public LinkedList<Point> getArea() {
        LinkedList<Point> area = new LinkedList<>();
        int cellX = x - width / 2;
        int cellY = y - height / 2;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                area.add(new Point(cellX, cellY));
                cellY++;
            }
            cellY = y - height / 2;
            cellX++;
        }
        return area;
    }

    private Player getClosestEnemy(LinkedList<State> states) {
        Player closest = null;
        int distance = Integer.MAX_VALUE;
        for (State state : states) {
            if (state.getName().equals("Player")) {
                Player player = (Player) state;
                if (!player.leave && !player.dead && player.team != team && inLine(player.x, player.y) && distanceTo(player.x, player.y) < distance) {
                    closest = player;
                }
            }
        }
        return closest;
    }

    private boolean inLine(int x, int y) {
        boolean inLine = false;
        if (this.x == x || this.y == y || Math.abs(this.x - x) == Math.abs(this.y - y)) {
            inLine = true;
        }
        return inLine;
    }

    private int distanceTo(int x, int y) {
        return (int) Math.sqrt((y - this.y) * (y - this.y) + (x - this.x) * (x - this.x));
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        LinkedList<State> newStates = new LinkedList<>();
        if (!dead) {
            Random random = new Random();
            if (random.nextInt(100) <= 40) { //porcentaje de que la torre dispare
                Player closestPlayer = getClosestEnemy(states);
                int xVelocity = 0;
                int yVelocity = 0;
                if (closestPlayer != null) {
                    int posX = closestPlayer.x;
                    int posY = closestPlayer.y;
                    if (posX != x || posY != y) {
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
                    }
                } else {
                    do {
                        xVelocity = random.nextInt(3) - 1;
                        yVelocity = random.nextInt(3) - 1;
                    } while (xVelocity == 0 && yVelocity == 0);
                }
                Projectile projectile = new Projectile(towerId, countProjectile, team, xVelocity, yVelocity, x, y, "Projectile", false, null);
                newStates.add(projectile);
                this.addEvent("fire");
            }
        }
        return newStates;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        hasChanged = false;
        int newCountProjectile = countProjectile;
        boolean newDead = dead;
        int newHealth = health;
        boolean newDestroy = destroy;
        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            hasChanged = true;
            for (String event : events) {
                switch (event) {
                    case "hit":
                        newHealth = health - 10;
                        if (newHealth <= 0) {
                            newDead = true;
                            System.out.println("Tower " + towerId + " has been killed.");
                        }
                        break;
                    case "fire":
                        newCountProjectile = countProjectile + 1;
                        //System.out.println("Tower " + id + " fired a projectile.");
                        break;
                    case "spawn":
                        System.out.println("Tower " + towerId + " spawn in game.");
                        break;
                    case "despawn":
                        newDestroy = true;
                        System.out.println("Tower " + towerId + " despawn of the game.");
                        break;
                }
            }
        }
        Tower newTower = new Tower(towerId, newCountProjectile, newDead, team, newHealth, healthMax, width, height, x, y, name, newDestroy, id);
        return newTower;
    }

    @Override
    public void setState(State newTower) {
        super.setState(newTower);
        towerId = ((Tower) newTower).towerId;
        countProjectile = ((Tower) newTower).countProjectile;
        dead = ((Tower) newTower).dead;
        team = ((Tower) newTower).team;
        health = ((Tower) newTower).health;
        healthMax = ((Tower) newTower).healthMax;
        width = ((Tower) newTower).width;
        height = ((Tower) newTower).height;
    }

    @Override
    protected Object clone() {
        Tower clon = new Tower(towerId, countProjectile, dead, team, health, healthMax, width, height, x, y, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonTower = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("id", towerId);
        jsonAttrs.put("countProjectile", countProjectile);
        jsonAttrs.put("dead", dead);
        jsonAttrs.put("team", team);
        jsonAttrs.put("health", health);
        jsonAttrs.put("healthMax", healthMax);
        jsonAttrs.put("width", width);
        jsonAttrs.put("height", height);
        jsonTower.put("Tower", jsonAttrs);
        return jsonTower;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        JSONObject superJSON = super.toJSON(sessionId, states, staticStates, actions, lastState);
        return superJSON != null && !isJSONRemover(superJSON) ? toJSON() : superJSON;
    }

}
