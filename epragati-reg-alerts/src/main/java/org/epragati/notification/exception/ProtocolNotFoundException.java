package org.epragati.notification.exception;

import org.epragati.notification.msg.Msg;

/**
 * 
 * @author pbattula
 *
 */

public class ProtocolNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String message = Msg.PROTOCOL_MISSING;
	
	public ProtocolNotFoundException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
