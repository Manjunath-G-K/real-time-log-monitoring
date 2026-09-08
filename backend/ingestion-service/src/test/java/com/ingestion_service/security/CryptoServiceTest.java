package com.ingestion_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private EncryptionKeyManager keyManager;
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        keyManager = new EncryptionKeyManager();
        cryptoService = new CryptoService(keyManager);
    }

    @Test
    void decryptingWhatWasEncrypted_returnsTheOriginalPlaintext() {
        String plaintext = "User ***@gmail.com logged in with phone ******3210";

        String encrypted = cryptoService.encrypt(plaintext);
        String decrypted = cryptoService.decrypt(encrypted);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void encryptedOutput_isNotThePlaintextAndIsNotReversibleWithoutTheKey() {
        String plaintext = "User ***@gmail.com logged in";

        String encrypted = cryptoService.encrypt(plaintext);

        assertFalse(encrypted.contains(plaintext), "ciphertext must not leak the plaintext");
        // This is the exact regression check for the bug we're fixing:
        // the old "encryption" was Base64(key + ":" + message), which
        // meant plain base64-decoding the output recovered the message
        // with no key at all. A real cipher's output must not do that.
        String naiveBase64Decode = new String(java.util.Base64.getDecoder().decode(
                encrypted.substring(encrypted.lastIndexOf(':') + 1)));
        assertNotEquals(plaintext, naiveBase64Decode);
    }

    @Test
    void encryptingTheSameMessageTwice_producesDifferentCiphertext() {
        String plaintext = "same message every time";

        String first = cryptoService.encrypt(plaintext);
        String second = cryptoService.encrypt(plaintext);

        // Proves the IV is actually random per call (semantic security) —
        // if it weren't, identical plaintexts would leak as identical
        // ciphertexts, which is itself information.
        assertNotEquals(first, second);
        assertEquals(cryptoService.decrypt(first), cryptoService.decrypt(second));
    }

    @Test
    void decryptingWithARotatedOutKey_throwsAClearError() {
        String encryptedUnderVersionOne = cryptoService.encrypt("will outlive its key");

        for (int i = 0; i < 10; i++) {
            keyManager.rotateKey();
        }

        EncryptionException thrown = assertThrows(EncryptionException.class,
                () -> cryptoService.decrypt(encryptedUnderVersionOne));
        assertTrue(thrown.getMessage().contains("rotated out"));
    }

    @Test
    void decryptingATamperedCiphertext_failsIntegrityCheckInsteadOfReturningGarbage() {
        String encrypted = cryptoService.encrypt("do not modify me");
        String tampered = encrypted.substring(0, encrypted.length() - 4) + "abcd";

        assertThrows(EncryptionException.class, () -> cryptoService.decrypt(tampered));
    }

    @Test
    void decryptingAMalformedPayload_throwsInsteadOfCrashingWithAnUncheckedException() {
        assertThrows(EncryptionException.class, () -> cryptoService.decrypt("not-a-valid-payload"));
    }
}
