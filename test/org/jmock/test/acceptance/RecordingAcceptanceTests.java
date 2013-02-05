package org.jmock.test.acceptance;

import static java.util.Arrays.asList;
import static org.hamcrest.StringDescription.asString;
import static org.junit.Assert.assertThat;
import junit.framework.TestCase;

import org.hamcrest.Matcher;
import org.hamcrest.text.StringContainsInOrder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.ExpectationError;

public class RecordingAcceptanceTests extends TestCase {
    Mockery context = new Mockery();
    MockedType mock = context.mock(MockedType.class, "mock");
    
    public void testRecordsActualInvocations() {
        context.checking(new Expectations() {{
            allowing (same(mock));
        }});
        
        mock.doSomething();
        mock.doSomethingWith("foo");
        mock.doSomethingWith("x", "y");
        
        assertThat(asString(context), containsInOrder(
            "what happened before this:",
            "mock.doSomething()",
            "mock.doSomethingWith(\"foo\")",
            "mock.doSomethingWith(\"x\", \"y\")"));
    }

    public void testClearHistory() {
        context.checking(new Expectations() {{
            allowing(mock).doSomething();
            oneOf(mock).doSomethingWith("foo");
            atMost(1).of(mock).doSomethingWith("x", "y");
            atMost(1).of(mock).doSomethingWith("X", "Y");
            never(mock).method1();
        }});

        assertThat(asString(context), containsInOrder(
            "expectations:",
            "allowed, never invoked: mock.doSomething()",
            "! expected once, never invoked: mock.doSomethingWith(\"foo\")",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"x\", \"y\")",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"X\", \"Y\")",
            "expected never, never invoked: mock.method1()",
            "what happened before this:",
            "nothing!"));

        context.clearHistory();
        assertThat(asString(context), containsInOrder(
            "expectations:",
            "allowed, never invoked: mock.doSomething()",
            "! expected once, never invoked: mock.doSomethingWith(\"foo\")",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"x\", \"y\")",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"X\", \"Y\")",
            "expected never, never invoked: mock.method1()",
            "what happened before this:",
            "nothing!"));
        
        mock.doSomething();
        mock.doSomethingWith("foo");
        mock.doSomethingWith("x", "y");

        assertThat(asString(context), containsInOrder(
            "expectations:",
            "allowed, already invoked 1 time: mock.doSomething()",
            "expected once, already invoked 1 time: mock.doSomethingWith(\"foo\")",
            "expected at most 1 time, already invoked 1 time: mock.doSomethingWith(\"x\", \"y\")",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"X\", \"Y\")",
            "expected never, never invoked: mock.method1()",
            "what happened before this:",
            "mock.doSomething()",
            "mock.doSomethingWith(\"foo\")",
            "mock.doSomethingWith(\"x\", \"y\")"));

        context.clearHistory();
        assertThat(asString(context), containsInOrder(
            "expectations:",
            "allowed, already invoked 1 time: mock.doSomething()",
            "expected at most 1 time, never invoked: mock.doSomethingWith(\"X\", \"Y\")",
            "expected never, never invoked: mock.method1()",
            "what happened before this:",
            "nothing!"));
    }

    static class ExampleException extends RuntimeException {}
    
    public void testRecordsInvocationsThatThrowExceptions() {
        context.checking(new Expectations() {{
            allowing (mock).doSomething(); will(throwException(new ExampleException()));
        }});
        
        try {
            mock.doSomething();
            fail("no exception thrown");
        }
        catch (ExampleException expected) {}
        
        assertThat(asString(context), containsInOrder(
            "what happened before this:",
            "mock.doSomething()"));
    }

    public void testDoesNotRecordUnexpectedInvocations() {
        context.checking(new Expectations() {{
            allowing (mock).doSomethingWith("foo");
        }});
        
        try {
            mock.doSomethingWith("bar");
        }
        catch (ExpectationError expected) {}
        
        assertThat(asString(context), containsInOrder(
            "what happened before this:", 
            "nothing"));
    }
    
    public void testReportsRecordedInvocationsWhenUnexpectedInvocationReceived() {
        context.checking(new Expectations() {{
            oneOf (mock).doSomethingWith("x");
            oneOf (mock).doSomethingWith("y");
        }});
        
        mock.doSomethingWith("y");
        
        try {
            mock.doSomethingWith("z");
            fail("should have reported unexpected invocation");
        }
        catch (ExpectationError e) {
            assertThat(asString(e), containsInOrder(
                "what happened before this:",
                "mock.doSomethingWith(\"y\")"
            ));
        }
    }
    
    public void testReportsRecordedInvocationsWhenNotSatisfied() {
        context.checking(new Expectations() {{
            oneOf (mock).doSomethingWith("x");
            oneOf (mock).doSomethingWith("y");
        }});
        
        mock.doSomethingWith("y");
        
        try {
            context.assertIsSatisfied();
            fail("should not be satisfied");
        }
        catch (ExpectationError e) {
            assertThat(asString(e), containsInOrder(
                "what happened before this:",
                "mock.doSomethingWith(\"y\")"
            ));
        }
    }
    
    private Matcher<? super String> containsInOrder(String... strings) {
        return new StringContainsInOrder(asList(strings));
    }
}
