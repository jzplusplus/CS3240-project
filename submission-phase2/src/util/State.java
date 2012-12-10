package util;

public class State implements Comparable<State> {
    private static int instanceCounter = 0;
    private String name;
    private boolean isAcceptingState;

    public State(State copy) {
        this(copy.name, copy.isAcceptingState, true);
    }
    public State() {
        this(generateName(), false, false);
    } 
    public State(boolean isAcceptingState) {
        this(generateName(), isAcceptingState, false);
    }
    public State(String name, boolean isAcceptingState, boolean isCopy) {
        this.name = name;
        this.isAcceptingState = isAcceptingState;
        if (!isCopy) {
            instanceCounter++;
        }
    }

    public boolean isAccepting() {
        return this.isAcceptingState;
    }

    public String getName() {
        return this.name;
    }

    public void setAccepting(boolean accepting) {
        this.isAcceptingState = accepting;
    }

    private static String generateName() {
        return "State-" + instanceCounter;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof State) {
            State other = (State) o;
            return this.name.equals(other.name) && this.isAcceptingState == other.isAcceptingState;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 37 + (this.isAcceptingState ? 1 : 0);
    }

    public int compareTo(State o) {
        return this.name.compareTo(o.name);
    }

    public String toString() {
        return "<State " + name + (isAcceptingState ? "(*)" : "") + ">";
    }
}
