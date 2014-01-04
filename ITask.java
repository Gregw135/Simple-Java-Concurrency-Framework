package processingFrameworks.Concurrency;

import processingFrameworks.Concurrency.Support.FutureResult;


/**
 * The public interface for Steps and Tasks.  
 * 
 * TODO: Reduce this interface to a single method:  Return<V> act(). Users should be able to submit any class to the task queue that
 * implements this method.
 * 
 * @author Greg Womack
 * gregw134@gmail.com
 * @V return type
 */
public interface ITask<V> {

	/**
	 * Get the FutureResult that corresponds to this task.
	 * @return
	 */
	public <K> FutureResult<K,V> getFuture();
	
	/**
	 * Submit this task to be processed. Returns false if the task can't be immediately added to the task queue.
	 */
	public boolean start();
	
	/**
	 * (For internal package use only).
	 * @return
	 */
	public IWorkable<V> getDangerousInnerClass();
	
}
