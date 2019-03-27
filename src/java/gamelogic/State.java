package gamelogic;

import java.util.HashMap;
import java.util.LinkedList;
import org.json.simple.JSONObject;

public abstract class State {

    protected String name;
    private LinkedList<State> record;
    private LinkedList<String> events;
    protected boolean hasChanged;
    protected boolean destroy;

    public State(String name, boolean destroy) {
        this.name = name;
        this.record = new LinkedList<>();
        this.events = new LinkedList<>();
        this.hasChanged = false;
        this.destroy = destroy;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        //TODO in concrete class
        return null;
    }

    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        //TODO in concrete class
        hasChanged = false;
        return this;
    }

    public void createState(State newState) {
        //record.add((State) this.clone());
        this.setState(newState);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState(int numState) {
        return record.get(numState);
    }

    public void setState(State newState) {
        name = newState.name;
        destroy = newState.destroy;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public JSONObject toJSON() {
        JSONObject jsonState = new JSONObject();
        JSONObject jsonAttrs = new JSONObject();
        jsonAttrs.put("name", name);
        jsonAttrs.put("destroy", destroy);
        jsonState.put("State", jsonAttrs);
        return jsonState;
    }
    
    public JSONObject toJSON(String sessionId) {
        return toJSON();
    }

    @Override
    protected Object clone() {
        //TODO in concrete class
        return null;
    }

    public void addEvent(String event) {
        events.add(event);
    }

    public LinkedList<String> getEvents() {
        return events;
    }

    public void clearEvents() {
        events.clear();
    }

}
