package org.jmock.internal;

import org.hamcrest.Description;
import org.hamcrest.SelfDescribing;
import org.jmock.api.Expectation;
import org.jmock.api.ExpectationError;
import org.jmock.api.Invocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InvocationDispatcher implements ExpectationCollector, SelfDescribing {
    private final Object lock;
    private final List<Expectation> expectations = new ArrayList<Expectation>();
    private final List<StateMachine> stateMachines = new ArrayList<StateMachine>();

    public InvocationDispatcher(Object lock) {
        this.lock = lock; 
    }

    public StateMachine newStateMachine(String name) {
        synchronized (lock) {
            StateMachine stateMachine = new StateMachine(name, lock);
            stateMachines.add(stateMachine);
            return stateMachine;
        }
    }
    
    @Override // from ExpectationCollector
    public void add(Expectation expectation) {
        synchronized (lock) {
            expectations.add(expectation);
        }
    }

    @Override // from SelfDescribing
    public void describeTo(Description description) {
        synchronized (lock) {
            describe(description, expectations);
        }
    }

    public void describeMismatch(Invocation invocation, Description description) {
        synchronized (lock) {
            describe(description, describedWith(expectations, invocation));
        }
    }

    private Iterable<SelfDescribing> describedWith(List<Expectation> expectations, final Invocation invocation) {
        final Iterator<Expectation> iterator = expectations.iterator();
        return new Iterable<SelfDescribing>() {
            public Iterator<SelfDescribing> iterator() {
                return new Iterator<SelfDescribing>() {
                    public boolean hasNext() { return iterator.hasNext(); }
                    public SelfDescribing next() {
                        return new SelfDescribing() {
                            public void describeTo(Description description) {
                                iterator.next().describeMismatch(invocation, description);
                            }
                        };
                    }
                    public void remove() { iterator.remove(); }
                };
            }
        };
    }

    private void describe(Description description, Iterable<? extends SelfDescribing> selfDescribingExpectations) {
        if (expectations.isEmpty()) {
            description.appendText("no expectations specified: did you...\n"+
                                   " - forget to start an expectation with a cardinality clause?\n" +
                                   " - call a mocked method to specify the parameter of an expectation?");
        }
        else {
            description.appendList("expectations:\n  ", "\n  ", "", selfDescribingExpectations);
            if (!stateMachines.isEmpty()) {
                description.appendList("\nstates:\n  ", "\n  ", "", stateMachines);
            }
        }
    }
    

    public boolean isSatisfied() {
        synchronized (lock) {
            for (Expectation expectation : expectations) {
                if (! expectation.isSatisfied()) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public Object dispatch(Invocation invocation) throws Throwable {
        Expectation matchingExpectation=null;
        synchronized (lock) {
            for (Expectation expectation : expectations) {
                if (expectation.matches(invocation)) {
                    expectation.preInvoke();
                    matchingExpectation=expectation;
                    break;
                }
            }
        }

        if (null!=matchingExpectation) {
            return matchingExpectation.invoke(invocation);
        } else {
            throw ExpectationError.unexpected("unexpected invocation", invocation);
        }
    }

    public void clearHistory() {
        synchronized (lock) {
            for (Iterator<Expectation> itr=expectations.iterator(); itr.hasNext(); ) {
                Expectation expectation=itr.next();
                if (expectation.isHistoric()) {
                    itr.remove();
                }
            }
        }
    }
}
