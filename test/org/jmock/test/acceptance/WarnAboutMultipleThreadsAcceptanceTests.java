package org.jmock.test.acceptance;

import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;

import junit.framework.TestCase;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.concurrent.Blitzer;
import org.jmock.lib.concurrent.SingleThreadedPolicy;

@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
public class WarnAboutMultipleThreadsAcceptanceTests extends TestCase {
    BlockingQueue<Throwable> exceptionsOnBackgroundThreads = new LinkedBlockingQueue<Throwable>();

    private ThreadFactory exceptionCapturingThreadFactory = new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    try {
                        exceptionsOnBackgroundThreads.put(e);
                    } catch (InterruptedException e1) {
                        throw new ThreadDeath();
                    }
                }
            });
            return t;
        }
    };

    Blitzer blitzer = new Blitzer(1, Executors.newFixedThreadPool(1, exceptionCapturingThreadFactory));

    public void testKillsThreadsThatTryToCallMockeryThatIsNotThreadSafe() throws InterruptedException {
        Mockery mockery = new Mockery();
        mockery.setThreadingPolicy(new SingleThreadedPolicy());
        
        final MockedType mock = mockery.mock(MockedType.class, "mock");
        
        mockery.checking(new Expectations() {{
            allowing (mock).doSomething();
        }});
        
        blitzer.blitz(new Runnable() {
            public void run() {
                mock.doSomething();
            }            
        });

        Throwable exception = exceptionsOnBackgroundThreads.take();
        assertThat(exception.getMessage(), stringContainsInOrder("Only thread", "is allowed to use the Mockery."));
    }
    
    @Override
    public void tearDown() {
        blitzer.shutdown();
    }
}
