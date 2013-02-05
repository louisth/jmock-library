package org.jmock.lib.concurrent;

import org.jmock.api.Invokable;
import org.jmock.api.ThreadingPolicy;

/**
 * A {@link ThreadingPolicy} that imposes no limits on concurrent access. 
 */
public class FreeThreadingPolicy implements ThreadingPolicy {

    @Override // from ThreadingPolicy
    public Invokable synchroniseAccessTo(Invokable mockObject) {
        return mockObject;
    }
}
