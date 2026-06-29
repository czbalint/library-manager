package org.czbalint.librarymanager.web.error;

public class InvalidLoanStateException extends RuntimeException {

    public InvalidLoanStateException(String message) {
        super(message);
    }
}
