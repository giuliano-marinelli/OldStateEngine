package phasergamelogic;

import engine.StaticState;
import engine.State;
import engine.Action;
import java.awt.Point;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Map extends StaticState {

    private HashMap<Point, Integer> cells;
    private int width;
    private int height;

    public Map(HashMap<Point, Integer> cells, int width, int height, String name, String id) {
        super(name, id == null ? UUID.randomUUID().toString() : id);
        this.cells = cells;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean canWalk(Point xy) {
        boolean res;
        res = (cells.containsKey(xy) && cells.get(xy) == 1);
        return res;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jsonMap = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        JSONArray jsonCells = new JSONArray();
        for (java.util.Map.Entry<Point, Integer> cell : cells.entrySet()) {
            Point key = cell.getKey();
            Integer value = cell.getValue();
            JSONObject jsonCell = new JSONObject();
            jsonCell.put("val", value);
            jsonCell.put("x", key.x);
            jsonCell.put("y", key.y);
            jsonCells.add(jsonCell);
        }
        jsonAttrs.put("cells", jsonCells);
        jsonAttrs.put("width", width);
        jsonAttrs.put("height", height);
        jsonMap.put("Map", jsonAttrs);
        return jsonMap;
    }

    @Override
    public JSONObject toJSON(String sessionId, LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, LinkedList<Action>> actions, JSONObject lastState) {
        return lastState == null ? toJSON() : null;
    }

}
