package com.ingestion_service.security;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionKeyManagerTest {

    @Test
    void startsAtVersionOneWithARealKey() {
        EncryptionKeyManager keyManager = new EncryptionKeyManager();

        assertEquals(1, keyManager.getCurrentVersion());
        assertNotNull(keyManager.getCurrentKey());
        assertEquals("AES", keyManager.getCurrentKey().getAlgorithm());
        // 256-bit key = 32 bytes.
        assertEquals(32, keyManager.getCurrentKey().getEncoded().length);
    }

    @Test
    void rotateKey_incrementsVersionAndChangesTheKey() {
        EncryptionKeyManager keyManager = new EncryptionKeyManager();
        SecretKey originalKey = keyManager.getCurrentKey();

        keyManager.rotateKey();

        assertEquals(2, keyManager.getCurrentVersion());
        assertNotEquals(originalKey, keyManager.getCurrentKey());
    }

    @Test
    void rotateKey_keepsRecentOldVersionsRetrievable() {
        EncryptionKeyManager keyManager = new EncryptionKeyManager();
        SecretKey versionOneKey = keyManager.getCurrentKey();

        keyManager.rotateKey(); // now on version 2

        // A log encrypted just before rotation must still be decryptable.
        assertTrue(keyManager.getKey(1).isPresent());
        assertEquals(versionOneKey, keyManager.getKey(1).get());
    }

    @Test
    void rotateKey_eventuallyEvictsVeryOldVersions() {
        EncryptionKeyManager keyManager = new EncryptionKeyManager();

        for (int i = 0; i < 10; i++) {
            keyManager.rotateKey();
        }

        // Version 1 is long gone by now — retention is bounded, not infinite.
        assertTrue(keyManager.getKey(1).isEmpty());
        // But the current key is always present.
        assertTrue(keyManager.getKey(keyManager.getCurrentVersion()).isPresent());
    }
}
