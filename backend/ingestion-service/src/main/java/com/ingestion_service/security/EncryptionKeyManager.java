package com.ingestion_service.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the lifecycle of the AES encryption key: generation, versioning,
 * and rotation.
 *
 * Responsibility: answer "what key do I encrypt with right now?" and
 * "what key was version N encrypted with?" — nothing about *how*
 * encryption itself works. That's {@link CryptoService}'s job. Splitting
 * these means key rotation policy can change (retention window, rotation
 * triggers) without touching a single line of cipher code, and the cipher
 * code can be unit-tested without needing to simulate rotation.
 *
 * Every key is versioned. Ciphertext produced under version N stores that
 * version number alongside it (see CryptoService), so rotating the key
 * never breaks the ability to decrypt data that was already encrypted —
 * as long as that version is still retained.
 */
@Component
public class EncryptionKeyManager {

    private static final Logger log = LoggerFactory.getLogger(EncryptionKeyManager.class);

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE_BITS = 256;

    /** How many past key versions we keep around for decrypting older logs. */
    private static final int RETAINED_KEY_VERSIONS = 5;

    private final Map<Integer, SecretKey> keysByVersion = new ConcurrentHashMap<>();
    private volatile int currentVersion;

    public EncryptionKeyManager() {
        this.currentVersion = 1;
        keysByVersion.put(currentVersion, generateKey());
        log.info("Initialized encryption key, version={}", currentVersion);
    }

    public SecretKey getCurrentKey() {
        return keysByVersion.get(currentVersion);
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Looks up the key for a specific version, so ciphertext encrypted
     * before a rotation can still be decrypted — provided that version
     * hasn't aged out of {@link #RETAINED_KEY_VERSIONS}.
     */
    public Optional<SecretKey> getKey(int version) {
        return Optional.ofNullable(keysByVersion.get(version));
    }

    /**
     * Generates a new key and makes it current. The previous key is kept
     * (bounded by {@link #RETAINED_KEY_VERSIONS}) so recently-written logs
     * remain decryptable — this method rotates the key forward, it does
     * not itself decide whether old logs should be invalidated. That's a
     * policy decision for whoever calls rotate() (today: panic mode).
     */
    public synchronized void rotateKey() {
        int previousVersion = currentVersion;
        int newVersion = previousVersion + 1;

        keysByVersion.put(newVersion, generateKey());
        currentVersion = newVersion;

        int oldestVersionToKeep = newVersion - RETAINED_KEY_VERSIONS;
        keysByVersion.keySet().removeIf(version -> version < oldestVersionToKeep);

        log.info("Rotated encryption key: version {} -> {}", previousVersion, newVersion);
    }

    private SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance(ALGORITHM);
            generator.init(KEY_SIZE_BITS);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            // AES is a mandatory algorithm on every standard JVM; this
            // branch exists only to satisfy the checked exception.
            throw new IllegalStateException("AES is not available on this JVM", e);
        }
    }
}
