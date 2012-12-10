package exception;

public class BadTokenException extends Exception {

	private String partialToken;
	
	public BadTokenException(String partial)
	{
		partialToken = partial;
	}
	
	public String getPartialToken()
	{
		return partialToken;
	}
}
