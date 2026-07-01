package org.czbalint.librarymanager.error;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Long id) {
        return new ResourceNotFoundException(type + " not found with id " + id);
    }
}
