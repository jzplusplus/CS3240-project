package util;

public class State {
    private static int instanceCounter = 0;
    private String name;
    private boolean isAcceptingState;
    public State() {
        this(generateName(), false);
    } 
    public State(boolean isAcceptingState) {
        this(generateName(), isAcceptingState);
    }
    public State(String name, boolean isAcceptingState) {
        this.name = name;
        this.isAcceptingState = isAcceptingState;
        instanceCounter++;
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

}
