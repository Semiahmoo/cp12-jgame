package jgame.impl;
/** Exception used for passing error conditions within JGame */
public class JGameError extends Error {
	private static final long serialVersionUID = 1090655020825667613L;
	String msg;
	boolean fatal=false;

	/** Construct non-fatal error */
	public JGameError(String message) {
		msg=message;
	}

	/** Construct fatal or non-fatal error */
	public JGameError(String message, boolean fatal) {
		msg=message;
		this.fatal=fatal;
	}

	public String toString() { return msg; }
}
