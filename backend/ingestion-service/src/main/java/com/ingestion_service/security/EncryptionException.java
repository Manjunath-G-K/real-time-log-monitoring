package com.ingestion_service.security;

/**
 * Wraps the checked crypto exceptions the JDK's Cipher API throws
 * (NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
 * AEADBadTagException, etc.) into one unchecked type.
 *
 * Responsibility: let callers (LogService, controllers) handle "encryption
 * failed" as a single case without needing to know or catch every
 * specific javax.crypto checked exception. An AEADBadTagException in
 * particular means GCM's integrity check failed — the ciphertext was
 * corrupted or tampered with — which is exactly the kind of failure a
 * caller should treat as "reject this data," not silently ignore.
 */
public class EncryptionException extends RuntimeException {

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(String message) {
        super(message);
    }
}
