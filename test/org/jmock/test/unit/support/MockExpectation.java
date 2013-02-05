package org.jmock.test.unit.support;


import org.hamcrest.Description;
import org.jmock.api.Expectation;
import org.jmock.api.Invocation;
import org.junit.Assert;

public class MockExpectation extends Assert implements Expectation {
    public boolean matches;
    public boolean isSatisfied;
    public boolean allowsMoreInvocations;

    public MockExpectation(boolean matches, boolean isSatisfied, boolean allowsMoreInvocations) {
        this.matches = matches;
        this.isSatisfied = isSatisfied;
        this.allowsMoreInvocations = allowsMoreInvocations;
    }
    
    @Override // from Expectation
    public boolean matches(Invocation invocation) {
        return matches;
    }

    @Override // from Expectation
    public boolean allowsMoreInvocations() {
        return allowsMoreInvocations;
    }

    @Override // from Expectation
    public boolean isHistoric() {
        throw new RuntimeException("Expected this statement to never be executed.");
    }

    @Override // from Expectation
    public boolean isSatisfied() {
        return isSatisfied;
    }
    
    private boolean shouldBeInvoked = true;
    private Invocation expectedInvocation = null;
    public Object invokeResult = null;
    public boolean wasInvoked = false;
    
    public void shouldNotBeInvoked() {
        shouldBeInvoked = false;
    }
    
    public void shouldBeInvokedWith(Invocation invocation) {
        shouldBeInvoked = true;
        expectedInvocation = invocation;
    }
    
    @Override // from Expectation
    public void preInvoke() {
        // nothing to do - validation will happen in invoke()
    }

    @Override // from Expectation
    public Object invoke(Invocation invocation) throws Throwable {
        assertTrue("should not have been invoked; invocation: " + invocation, 
                   shouldBeInvoked);
        
        if (expectedInvocation != null) {
            assertSame("unexpected invocation", expectedInvocation, invocation);
        }
        wasInvoked = true;
        return invokeResult;
    }

    @Override // from SelfDescribing
    public void describeTo(Description description) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override // from Expectation
    public void describeMismatch(Invocation invocation, Description description) {
        throw new UnsupportedOperationException("not implemented");
    }
}
