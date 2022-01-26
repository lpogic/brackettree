package brackettree.reader;

public class BracketTreeReadException extends RuntimeException {

    public BracketTreeReadException() {
    }

    public BracketTreeReadException(String message) {
        super(message);
    }

    public BracketTreeReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public BracketTreeReadException(Throwable cause) {
        super(cause);
    }

    public BracketTreeReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
