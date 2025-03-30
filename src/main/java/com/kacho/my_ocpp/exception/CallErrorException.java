package com.kacho.my_ocpp.exception;


public class CallErrorException extends RuntimeException {

    private final String message;

    public CallErrorException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
