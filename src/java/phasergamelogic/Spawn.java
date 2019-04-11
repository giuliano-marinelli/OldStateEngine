package phasergamelogic;

import engine.StaticState;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONObject;

public class Spawn extends StaticState {

    protected int x;
    protected int y;

    public Spawn(int x, int y, String name, String id) {
        super(name, id == null ? UUID.randomUUID().toString() : id);
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
    public JSONObject toJSON() {
        JSONObject jsonSpawn = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("super", super.toJSON());
        jsonAttrs.put("x", x);
        jsonAttrs.put("y", y);
        jsonSpawn.put("Spawn", jsonAttrs);
        return jsonSpawn;
    }

}
