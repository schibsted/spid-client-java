package no.spid.api.exceptions;

public class SpidOAuthException extends Exception {
    public SpidOAuthException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SpidOAuthException(String s) {
        super(s);
    }

    public SpidOAuthException(Throwable throwable) {
        super(throwable);
    }
}