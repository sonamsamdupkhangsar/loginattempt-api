package me.sonam.siteaccess;

public class FailedLoginException extends RuntimeException {
    public FailedLoginException(String message) {
        super(message);
    }
}
