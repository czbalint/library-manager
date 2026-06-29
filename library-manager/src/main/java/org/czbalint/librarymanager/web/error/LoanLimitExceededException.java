package org.czbalint.librarymanager.web.error;

public class LoanLimitExceededException extends RuntimeException {

    public LoanLimitExceededException(String message) {
        super(message);
    }
}
