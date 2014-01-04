package processingFrameworks.Concurrency;

import processingFrameworks.Concurrency.Support.FutureResult;



/**
 * (Classes implementing this interface should remain private to this package).
 * The interface between the Task Queue and a Task. Implementing classes need to provide an act() functionality, whose Status return
 * signals to the TaskQueue's workers whether the Task is complete (RETURN), or incomplete (RETRYNOW or RETRYLATER). TaskWorkers
 * will remove completed tasks from the task queue.
 * @author Greg Womack
 * gregw134@gmail.com
 *
 */
public interface IWorkable<V> {
	
	/**
	 * Do something. 
	 * Note: Implementing classes are entirely responsible for populating the future with data and exceptions. The only
	 * thing task worker will do is retry the method according to the status response returned, and remove the IWorkable 
	 * when Status.complete is returned or an exception is thrown.
	 * and exceptions. 
	 */
	public Return<V> doAction() throws Exception;
	
	public FutureResult<?,V> getFuture();
	
	public TaskQueue getTaskQueue();
	
	public void setTaskQueue(TaskQueue queue);
	
	/**
	 * Has this been submitted to an executor?
	 * @return
	 */
	public boolean isStarted();
	
	/**
	 * Indicate that a task is (or isn't) running in a TaskQueue.
	 * @param isStarted
	 */
	public void setStarted(boolean isStarted);
	
	public void setException(Exception e);
	
	
}
