package processingFrameworks.Concurrency;

import processingFrameworks.Concurrency.Support.FutureResult;



/**
 * (Classes implementing this interface should remain private to this package).
 * The least behavior required to implement a task that supports multiple steps:
 * -Return a status to the StepWorker
 * -Provide a return value V
 * -Be able to throw an exception
 * And:
 * -Be able to add new steps.
 * -Be able to cancel unprocessed steps.
 * -Be able to propagate exceptions to all unprocessed steps.
 * @author Greg Womack
 * gregw134@gmail.com
 *
 */
interface IWorkableMultiple<V> extends IWorkable<V>{

	/**
	 * This will be called for every step, and should delegate to the next child step. If that
	 * steps requests a retry, you should retry; if that step signals complete, you should move on 
	 * to the next step. Don't return Status.Complete until the last step has been processed.
	 */
	public Return<V> doAction() throws Exception;
	
	public <K> FutureResult<?,K> addTask(ITask<K> toAdd);
	@Override
	public FutureResult<?,V> getFuture();
	
}
