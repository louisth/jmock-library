package org.jmock.lib.concurrent;

import java.util.ConcurrentModificationException;

import org.jmock.api.Invocation;
import org.jmock.api.Invokable;
import org.jmock.api.ThreadingPolicy;

public class SingleThreadedPolicy implements ThreadingPolicy {
    private final Thread testThread;
    
    public SingleThreadedPolicy() {
        this.testThread = Thread.currentThread();
    }

    public Invokable synchroniseAccessTo(final Invokable mockObject) {
        return new Invokable() {
            public Object invoke(Invocation invocation) throws Throwable {
                checkRunningOnTestThread();
                return mockObject.invoke(invocation);
            }
        };
    }
    
    private void checkRunningOnTestThread() {
        if (Thread.currentThread() != testThread) {
            reportError("Only thread "+testThread+" is allowed to use the Mockery.");
        }
    }
    
    private void reportError(String error) {
        System.err.println(error);
        throw new ConcurrentModificationException(error);
    }
}
