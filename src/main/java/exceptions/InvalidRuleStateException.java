package exceptions;

public class InvalidRuleStateException extends Exception {

	public InvalidRuleStateException() {
		super();
	}
	
	public InvalidRuleStateException(String message) {
		super(message);
	}
}
