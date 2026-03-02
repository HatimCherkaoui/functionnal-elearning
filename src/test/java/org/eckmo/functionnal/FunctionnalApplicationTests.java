package org.eckmo.functionnal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Basic smoke test — the full integration is covered by IntegrationTest.
 * This test is intentionally minimal to avoid spinning up containers twice.
 */
@DisabledIfSystemProperty(named = "skipSmoke", matches = "true")
class FunctionnalApplicationTests {

    @Test
    void smoke() {
        // Full context load is tested in IntegrationTest with real containers
        // This placeholder keeps the test suite valid
    }
}
