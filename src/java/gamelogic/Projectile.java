package gamelogic;

import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public class Projectile extends Entity {

    protected String playerId;
    protected int number;
    protected int team;
    protected int xVelocity;
    protected int yVelocity;

    public Projectile(String playerId, int number, int team, int xVelocity, int yVelocity,
            int x, int y,
            String name, boolean destroy, String id) {
        super(x, y, name, destroy, id == null ? UUID.randomUUID().toString() : id);
        this.playerId = playerId;
        this.number = number;
        this.team = team;
        this.xVelocity = xVelocity;
        this.yVelocity = yVelocity;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        //si golpea a un jugador le quita vida
        //int newX = x + xVelocity;
        //int newY = y + yVelocity;
        for (State state : states) {
            if (state.getName().equals("Player") && !((Player) state).dead && !((Player) state).leave) {
                Player player = ((Player) state);
                //Point futurePosition = player.futurePosition(actions);
                //int playerxVelocity = futurePosition.x - player.x;
                //int playeryVelocity = futurePosition.y - player.y;
                if (x == player.x && y == player.y && team != player.team) {
                    //        || (futurePosition.x == newX && futurePosition.y == newY)
                    //        || ()) {
                    state.addEvent("hit");
                    this.addEvent("collide");
                }
            } else if (state.getName().equals("Tower") && !((Tower) state).dead) {
                Tower tower = ((Tower) state);
                if (team != tower.team) {
                    LinkedList<Point> areaTower = tower.getArea();
                    int i = 0;
                    boolean hit = false;
                    while (i < areaTower.size() && !hit) {
                        if (areaTower.get(i).x == x && areaTower.get(i).y == y) {
                            hit = true;
                            state.addEvent("hit");
                            this.addEvent("collide");
                        }
                        i++;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions) {
        hasChanged = true;
        int newX = x + xVelocity;
        int newY = y + yVelocity;
        boolean newDestroy = destroy;
        if (!((Map) staticStates.get(0)).canWalk(new Point(newX, newY))) {
            //si llega a una pared
            newX = x;
            newY = y;
            newDestroy = true;
        }
        LinkedList<String> events = getEvents();
        if (!events.isEmpty()) {
            hasChanged = true;
            for (String event : events) {
                switch (event) {
                    case "collide":
                        newDestroy = true;
                        break;
                }
            }
        }
        Projectile newArrow = new Projectile(playerId, number, team, xVelocity, yVelocity, newX, newY, name, newDestroy, id);
        return newArrow;
    }

    @Override
    public void setState(State newProjectile) {
        super.setState(newProjectile);
        playerId = ((Projectile) newProjectile).playerId;
        number = ((Projectile) newProjectile).number;
        team = ((Projectile) newProjectile).team;
        xVelocity = ((Projectile) newProjectile).xVelocity;
        yVelocity = ((Projectile) newProjectile).yVelocity;
    }

    @Override
    protected Object clone() {
        Projectile clon = new Projectile(playerId, number, team, xVelocity, yVelocity, x, y, name, destroy, id);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonArrow = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("id", playerId);
        jsonAttrs.put("number", number);
        jsonAttrs.put("team", team);
        jsonAttrs.put("xVelocity", x);
        jsonAttrs.put("yVelocity", y);
        jsonArrow.put("Projectile", jsonAttrs);
        return jsonArrow;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        JSONObject superJSON = super.toJSON(sessionId, states, staticStates, actions, lastState);
        return superJSON != null && !isJSONRemover(superJSON) ? toJSON() : superJSON;
    }

}
