package gamelogic;

import java.util.HashMap;
import java.util.LinkedList;

public abstract class StaticState extends State {

    public StaticState(String name) {
        super(name, false);
    }

    @Override
    public State next(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        return this;
    }

    @Override
    public LinkedList<State> generate(LinkedList<State> states, LinkedList<StaticState> staticStates, HashMap<String, Action> actions) {
        return null;
    }

    @Override
    public void createState(State newState) {
        //do nothing
    }

    @Override
    public State getState(int numState) {
        return this;
    }

    @Override
    public void setState(State newState) {
        //do nothing
    }

}
