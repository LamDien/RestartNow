package com.car.restartnow;

import org.junit.Test;
import static org.junit.Assert.*;

public class RebootManagerTest {

    @Test
    public void testRebootCallbackInterface() {
        final boolean[] initiatedCalled = {false};
        final boolean[] failedCalled = {false};
        final boolean[] setupCalled = {false};

        RebootManager.RebootCallback callback = new RebootManager.RebootCallback() {
            @Override
            public void onRebootInitiated() {
                initiatedCalled[0] = true;
            }

            @Override
            public void onAccessibilitySetupRequired() {
                setupCalled[0] = true;
            }

            @Override
            public void onRebootFailed(String errorMessage) {
                failedCalled[0] = true;
            }
        };

        callback.onRebootInitiated();
        assertTrue("Callback onRebootInitiated should be callable", initiatedCalled[0]);

        callback.onAccessibilitySetupRequired();
        assertTrue("Callback onAccessibilitySetupRequired should be callable", setupCalled[0]);

        callback.onRebootFailed("Test error");
        assertTrue("Callback onRebootFailed should be callable", failedCalled[0]);
    }
}
