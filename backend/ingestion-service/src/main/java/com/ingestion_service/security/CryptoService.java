package com.ingestion_service.security;

import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Performs the actual encrypt/decrypt operation — AES in Galois/Counter
 * Mode (GCM).
 *
 * Responsibility: turn plaintext into a self-describing, tamper-evident
 * ciphertext string, and back. Deliberately knows nothing about *which*
 * key to use beyond asking {@link EncryptionKeyManager} — key lifecycle
 * is not this class's concern, which is what makes this class simple
 * enough to unit-test with a plain instance, no Spring context needed.
 *
 * Why AES-GCM specifically (over, say, AES/CBC):
 *   - It's authenticated encryption: decrypting also verifies the
 *     ciphertext hasn't been altered. AES/CBC gives you confidentiality
 *     only — you'd need a separate HMAC to detect tampering (the classic
 *     "encrypt-then-MAC" pattern GCM does for you in one primitive).
 *   - It needs no padding, which removes an entire historical class of
 *     padding-oracle attacks that plagued CBC-mode implementations.
 *
 * Output format: "<keyVersion>:<base64 IV>:<base64 ciphertext+tag>" — a
 * deliberately simple, greppable envelope (no JSON dependency needed for
 * something this small) that carries everything decrypt() needs to find
 * the right key and verify integrity, without a separate lookup.
 */
@Component
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /** NIST SP 800-38D recommends a 96-bit (12-byte) IV for GCM. */
    private static final int GCM_IV_LENGTH_BYTES = 12;

    /** Authentication tag length in bits, appended to the ciphertext by the cipher itself. */
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private static final String FIELD_SEPARATOR = ":";

    private final EncryptionKeyManager keyManager;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(EncryptionKeyManager keyManager) {
        this.keyManager = keyManager;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            SecretKey key = keyManager.getCurrentKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return keyManager.getCurrentVersion()
                    + FIELD_SEPARATOR + Base64.getEncoder().encodeToString(iv)
                    + FIELD_SEPARATOR + Base64.getEncoder().encodeToString(ciphertext);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException("Failed to encrypt message", e);
        }
    }

    public String decrypt(String encoded) {
        String[] parts = encoded.split(FIELD_SEPARATOR, 3);
        if (parts.length != 3) {
            throw new EncryptionException("Malformed encrypted payload: expected 3 fields, got " + parts.length);
        }

        int keyVersion;
        try {
            keyVersion = Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            throw new EncryptionException("Malformed encrypted payload: invalid key version '" + parts[0] + "'");
        }

        SecretKey key = keyManager.getKey(keyVersion)
                .orElseThrow(() -> new EncryptionException(
                        "No key retained for version " + keyVersion + " — it was likely rotated out"));

        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            // GCM's built-in integrity check failed: either the wrong key,
            // or the ciphertext was modified after encryption.
            throw new EncryptionException("Ciphertext failed integrity check (tampered or wrong key)", e);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new EncryptionException("Failed to decrypt message", e);
        }
    }
}
