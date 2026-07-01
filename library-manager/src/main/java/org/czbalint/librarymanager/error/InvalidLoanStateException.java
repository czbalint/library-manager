package org.czbalint.librarymanager.error;

public class InvalidLoanStateException extends RuntimeException {

    public InvalidLoanStateException(String message) {
        super(message);
    }
}
