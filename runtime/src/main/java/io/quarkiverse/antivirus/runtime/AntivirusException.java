package io.quarkiverse.antivirus.runtime;

/**
 * Exception raised when a malicious vile is detected.
 */
public class AntivirusException extends RuntimeException {

    private final String fileName;

    public AntivirusException(final String fileName, final String message) {
        super(message);
        this.fileName = fileName;
    }

    public AntivirusException(final String fileName, final String message, final Throwable cause) {
        super(message, cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

}