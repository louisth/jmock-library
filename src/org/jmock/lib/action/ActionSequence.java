/*  Copyright (c) 2000-2007 jMock.org
 */
package org.jmock.lib.action;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.ExpectationError;
import org.jmock.api.Invocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Returns the next of a sequence of elements each time it is invoked.
 * 
 * @author nat
 *
 */
public class ActionSequence implements Action {
    private final Object lock = new Object();
    private final List<Action> actions;
    private final Iterator<Action> iterator;
    
    public ActionSequence(Action... actions) {
        this.actions = new ArrayList<Action>(Arrays.asList(actions));
        this.iterator = this.actions.iterator();
    }
    
    public Object invoke(Invocation invocation) throws Throwable {
        Action action;
        synchronized (lock) {
            if (!iterator.hasNext()) {
                throw ExpectationError.unexpected("no more actions available", invocation);
            }
            action=iterator.next();
        }
        return action.invoke(invocation);
    }
    
    public void describeTo(Description description) {
        description.appendList("", ", and then ", "", actions);
    }
}
