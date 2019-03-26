package gamelogic;

import java.util.HashMap;
import java.util.LinkedList;
import org.json.simple.JSONObject;

public class Entity extends State {

    protected int x;
    protected int y;

    public Entity(int x, int y, String name, boolean destroy) {
        super(name, destroy);
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        return null;
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        hasChanged = false;
        Entity newEntity = new Entity(x, y, name, destroy);
        return newEntity;
    }

    @Override
    public void setState(State newEntity) {
        super.setState(newEntity);
        x = ((Entity) newEntity).x;
        y = ((Entity) newEntity).y;
    }

    @Override
    protected Object clone() {
        Entity clon = new Entity(x, y, name, destroy);
        return clon;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonEntity = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("x", x);
        jsonAttrs.put("y", y);
        jsonEntity.put("Entity", jsonAttrs);
        return jsonEntity;
    }

}
