package brackettree.writer;

public class BracketTreeWriteException extends Exception {

    public BracketTreeWriteException() {
    }

    public BracketTreeWriteException(String message) {
        super(message);
    }

    public BracketTreeWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public BracketTreeWriteException(Throwable cause) {
        super(cause);
    }

    public BracketTreeWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
