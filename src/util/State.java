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

    private static String generateName() {
        return "State-" + instanceCounter;
    }

}
