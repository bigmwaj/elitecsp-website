package ca.elitecsp.cms.service;

/**
 * Unchecked exception thrown when XML content cannot be parsed.
 *
 * <p>The handler catches this exception and returns a 500 response to the caller.
 */
public class XmlParsingException extends RuntimeException {

    public XmlParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
