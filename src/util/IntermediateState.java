package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntermediateState {

	private static int instanceCounter = 0;
	private String name;
	private Set<State> memberStates;
	private Map<Character, IntermediateState> transitionMap;
	boolean marker;
	boolean errorState;
	boolean startState;
	boolean isAccepting;



	public IntermediateState(String name, Set<State> memberStates, boolean isAcceptingState) {
		this.name = name;
		this.isAccepting = isAcceptingState;
		this.memberStates = memberStates;
		transitionMap = new HashMap<Character, IntermediateState>();
		marker = false;
		errorState = false;
		instanceCounter++;

	}

	public IntermediateState(Set<State> memberStates, boolean isAcceptingState) {
		this(generateName(), memberStates, isAcceptingState);

	}

	public Set<State> getMemberStates() {
		return memberStates;
	}

	/**
	 * If memberStates of this state is null, then the state is the error state
	 * where all questionable transitions wind up.
	 * 
	 * @param memberStates
	 * @return true if the input set of states is same as the members of this intermediate state
	 */
	public boolean isMember(Set<State> memberStates) {
		return this.getErrorState() ?  false : this.memberStates.equals(memberStates);
	}

	public void addMembers(Set<State> newMembers) {
		memberStates.addAll(newMembers);
	}

	public boolean isAccepting() { return isAccepting; }

	public void addMembers(State newMember) {
		memberStates.add(newMember);
	}

	public void addTransition(Character input, IntermediateState to) {
		transitionMap.put(input, to);
	}

	public IntermediateState getTransition(Character input) {
		return transitionMap.get(input);
	}

	public boolean getMarker() {
		return marker;
	}

	public boolean isMarked() {
		return marker;
	}
	public void mark(boolean marker) {
		this.marker = marker;
	}


	public void setMarker(boolean marker) {
		this.marker = marker;
	}

	public boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(boolean isErrorState) {
		this.errorState = isErrorState;
	}



	public void setStartState(boolean startState) {
		this.startState = startState;
	}

	public boolean isStartState() {
		return startState;
	}





	public String getName() {
		return this.name;
	}



	private static String generateName() {
		return "State-" + instanceCounter;
	}
	
	@Override
    public boolean equals(Object o) {
        if (o instanceof IntermediateState) {
            IntermediateState other = (IntermediateState) o;
            return this.name.equals(other.getName()) && this.isAccepting == other.isAccepting() && this.memberStates.equals(other.getMemberStates());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() * 37 + (this.isAccepting ? 1 : 0);
    }

    public int compareTo(IntermediateState o) {
        return this.name.compareTo(o.name);
    }

    public String toString() {
        return "<State " + name + (isAccepting ? "(*)" : "") + ">";
    }

} // end private class IntermediateState