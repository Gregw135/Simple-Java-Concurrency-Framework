package processingFrameworks.Concurrency.Support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple alternative to Java 8's completableFuture. There are 3 notable features:
 * -setResult() and setException() allows a result or exception to be set directly.
 * -addEvent() sets an event which will be run upon completion.
 * -getInput() returns an input value of type K which can be passed in during construction. It sometimes comes in handy.
 * @author Greg Womack
 * gregw134@gmail.com
 * 
 * TODO: support cancellation.
 * 
 * @param <V>
 */
public class FutureResult<K, V> implements Future<V> {

	private volatile Exception e = null;
	private final K input;
	private volatile V result = null;
	private final CountDownLatch latch = new CountDownLatch(1);
	private final AtomicBoolean isDone = new AtomicBoolean(false);
	private volatile BeforeReturnEvent<K,V> event;
	
	public FutureResult(K input){
		this.input = input;
		this.event = null;
	}
	
	/**
	 * Add an event to occur right before results are returned.
	 * Warning: Setting this method twice will cause a runtime exception
	 * to be thrown.
	 */
	protected synchronized void addEvent(BeforeReturnEvent<K,V> newEvent) throws Exception{
		if(this.event != null)
			throw new RuntimeException("The beforeReturn event handler has already been assigned.");
		this.event= newEvent;
	}
	
	public K getInput(){
		return this.input;
	}
	
	public synchronized boolean hasException(){
		return (e != null);
	}
	
	/*
	 * Submit the final result and return to the calling method.
	 */
	public void setResult(V toSubmit){
		if(isDone.get() == true)
			return;
		this.result = toSubmit;
		if(event != null){
			try{
				event.doBeforeReturn(this, toSubmit);
			}catch(Exception e){
				this.e = e;
			}
		}
		isDone.set(true);
		latch.countDown();
	}
	
	/*
	 * Return an exception to the calling method. If the future has already been returned,
	 * no exception can be set.
	 */
	public synchronized void setException(Exception toThrow){
		if(isDone.get() == true)
			return;
		e = toThrow;
		isDone.set(true);
		latch.countDown();
	}
	
	@Deprecated
	public boolean cancel(boolean arg0) {
		throw new RuntimeException("This method isn't supported.");
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		latch.await();
		if(e != null)
			throw new ExecutionException(e);
		return result;
	}

	public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException{
		latch.await(arg0, arg1);
		if(e != null)
			throw new ExecutionException(e);
		if(!isDone())
			throw new TimeoutException();
		return result;
	}

	@Deprecated
	public boolean isCancelled() {
		throw new RuntimeException("This method isn't supported.");
	}
	
	/**
	 * Has the implementing class submitted results yet?
	 * @return
	 */
	public synchronized boolean resultSubmitted(){
		return ((result != null)||(e!= null));
	}
	
	@Override
	public boolean isDone() { 
		return isDone.get();
	}
	
	public Exception getException(){
		return e;
	}
	
	/**
	 * A method that executes right before results are returned. 
	 * Set by overriding beforeReturn() in Request.
	 * @author Greg
	 *
	 */
	public interface BeforeReturnEvent<K,V>{
		public void doBeforeReturn(FutureResult<K,V> futureToBeReturned, V dataToReturn);
	}
	
}
