/* Copyright (c) 2000-2003, jMock.org. See LICENSE.txt */
package org.jmock.dynamock;

import org.jmock.Constraint;
import org.jmock.Verifiable;
import org.jmock.dynamic.BuildableInvokable;
import org.jmock.dynamic.CoreMock;
import org.jmock.dynamic.DynamicMock;
import org.jmock.dynamic.InvocationMatcher;
import org.jmock.dynamic.Invokable;
import org.jmock.dynamic.LIFOInvocationDispatcher;
import org.jmock.dynamic.Stub;
import org.jmock.dynamic.matcher.ArgumentsMatcher;



public class Mock 
	implements Verifiable 
{
    private DynamicMock coreMock;
    private BuildableInvokableFactory factory;
    
    
    public Mock( Class mockedClass ) {
    	this( mockedClass, CoreMock.mockNameFromClass(mockedClass) );
    }
    
    public Mock( Class mockedClass, String nonDefaultName ) {
        this( new CoreMock( mockedClass, 
        					nonDefaultName, 
        					new LIFOInvocationDispatcher() ), 
    		  new InvokableFactory() );
    }
    
	public Mock( DynamicMock coreMock, BuildableInvokableFactory factory ) {
		this.factory = factory;
		this.coreMock = coreMock;
	}
	
	public String toString() {
        return coreMock.toString();
    }
    
	public Object proxy() {
		return coreMock.proxy();
	}
	
	public void reset() {
		coreMock.reset();
	}
	
	public void verify() {
		coreMock.verify();
	}
	
    public void setDefaultStub( Stub newDefaultStub ) {
        coreMock.setDefaultStub(newDefaultStub);
    }
	
	/**
	 * Adds an invokable object to the mock object. 
	 * This is the fundamental hook for adding application-specific mock behaviour.
	 * All the sugar methods cause an eventual call to this method.
	 * 
	 * @param invokable The invokable object to add to the mock
	 */
	public void add(Invokable invokable) {
		coreMock.add(invokable);
	}

	public void stub(String methodName, InvocationMatcher argumentsMatcher, Stub stub ) {
		createStub( methodName, argumentsMatcher, stub );
	}
	
	public void stubVoid( String methodName, InvocationMatcher argumentsMatcher ) {
		stub( methodName, argumentsMatcher, factory.createVoidStub() );
	}

	public void stubAndReturn( String methodName, InvocationMatcher argumentsMatcher, 
							   Object result) 
	{
		stub( methodName, argumentsMatcher, factory.createReturnStub(result) );
	}

	public void stubAndThrow( String methodName, InvocationMatcher argumentsMatcher, 
							  Throwable throwable) 
	{
		stub( methodName, argumentsMatcher, factory.createThrowStub(throwable) );
	}
	
	public void expect( String methodName, InvocationMatcher argumentsMatcher, Stub stub ) {
		BuildableInvokable buildableInvokable = createStub( methodName, argumentsMatcher, stub );
		
		buildableInvokable.addMatcher( factory.createCallOnceMatcher() );
	}
	
	public void expectVoid( String methodName, InvocationMatcher argumentsMatcher ) {
		expect( methodName, argumentsMatcher, factory.createVoidStub() );
	}
	
	public void expectAndReturn( String methodName, InvocationMatcher argumentsMatcher, 
								 Object result ) 
	{
		expect( methodName, argumentsMatcher, factory.createReturnStub(result) );
	}
	
	public void expectAndThrow( String methodName, InvocationMatcher argumentsMatcher, 
								Throwable throwable ) 
	{
		expect( methodName, argumentsMatcher, factory.createThrowStub(throwable) );
	}

	/*------------------------------------------------------------------------------------------
	 *  THE FOLLOWING METHODS ARE NOT UNIT TESTED
	 */
	
    /**
     * @deprecated Not required, as if methodName is called, you will get an exception
     */
    public void expectNotCalled(String methodName) {
    }
    
    private InvocationMatcher createInvocationMatcher(Constraint[] constraints) {
    	return new ArgumentsMatcher(constraints);
    }
    
    private InvocationMatcher createInvocationMatcher(Constraint constraint) {
    	return createInvocationMatcher(new Constraint[]{constraint});
    }
    
    private InvocationMatcher createInvocationMatcher(Object argumentValue) {
    	return createInvocationMatcher(C.eq(argumentValue));
    }
    
    private BuildableInvokable createStub(String methodName, InvocationMatcher argumentsMatcher, Stub stub) {
    	BuildableInvokable buildableInvokable = factory.createBuildableInvokable();
    	
    	buildableInvokable.addMatcher( factory.createMethodNameMatcher(methodName) );
    	buildableInvokable.addMatcher( argumentsMatcher );
    	buildableInvokable.setStub(stub);
    	
    	add(buildableInvokable);
    	
    	return buildableInvokable;
    }
}