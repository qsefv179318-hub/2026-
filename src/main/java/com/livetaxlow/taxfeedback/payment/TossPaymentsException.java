package com.livetaxlow.taxfeedback.payment;

public class TossPaymentsException extends RuntimeException {

    private final String code;

    public TossPaymentsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
