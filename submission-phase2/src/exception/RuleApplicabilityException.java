package exception;

public class RuleApplicabilityException extends Exception {

	public RuleApplicabilityException() {
		super("There is no applicable rule.");
	}
}
