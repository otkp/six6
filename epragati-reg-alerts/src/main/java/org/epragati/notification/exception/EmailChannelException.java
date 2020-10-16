 package org.epragati.notification.exception;


public class EmailChannelException extends Exception {

    private static final long serialVersionUID = -4514171025554853015L;

    public EmailChannelException() { 
        super();
    }
    
    public EmailChannelException(Throwable t) {
        super(t);
    }
    
    public EmailChannelException(String message) {
        super(message);
    }

    public EmailChannelException(String message, Throwable cause) {
        super(message, cause);
    }

}
