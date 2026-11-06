package org.delcom.ifs25005pbo2026springbootcashflow.domain.exception;

/**
 * Exception yang dilempar saat validasi aturan bisnis atau input gagal.
 */
public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
