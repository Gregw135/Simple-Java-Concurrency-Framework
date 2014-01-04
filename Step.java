package processingFrameworks.Concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import processingFrameworks.Concurrency.Support.FutureResult;

/**
 * A slightly more light-weight implementation of ITask than the Task class that doesn't support adding steps. 
 * 
 * @author Greg Womack
 * gregw134@gmail.com
 *
 */
public abstract class Step<V> implements ITask<V>{
	
	private final FutureResult<?,V> future;
	private final InnerStep innerStep;
	private volatile TaskQueue queue;
	
	public Step(){
		this.future = new FutureResult<Void,V>(null);
		this.innerStep = new InnerStep(future);
		this.queue = null;//TaskManager.queueWhereBlockingIsOk;
	}
	
	/**
	 * Use this constructor if you want to pass in some value K to your futureResult. 
	 * The FutureResult will take on type <K,V>.
	 * @param <K>: Pass in some value K to be stored
	 * @param input
	 */
	public <K> Step(K input){
		this.future = new FutureResult<K,V>(input);
		this.innerStep = new InnerStep(future);
		this.queue = null;//TaskManager.queueWhereBlockingIsOk;
	}
	
	/**
	 * Use this constructor if you want to pass in some value K to your futureResult. 
	 * The FutureResult will take on type <K,V>.
	 * @param <K>: Pass in some value K to be stored
	 * @param input
	 */
	public <K> Step(K input, TaskQueue queue){
		this.future = new FutureResult<K,V>(input);
		this.innerStep = new InnerStep(future);
		this.queue = queue;
	}
	
	/**
	 * Use this constructor if you want to pass in some value K to your futureResult. 
	 * The FutureResult will take on type <K,V>.
	 * @param queue: The task queue where this step will be executed.
	 * @param input
	 */
	public Step(TaskQueue queue){
		this.future = new FutureResult<Void,V>(null);
		this.innerStep = new InnerStep(future);
		this.queue = queue;
	}
	
	
	
	/**
	 * Return a value V and a status indicating the progress of this step: RETRYNOW, RETRYLATER, COMPLETE.
	 * @return
	 * @throws Exception
	 */
	protected abstract Return<V> act() throws Exception;

	public FutureResult<?, V> getFuture() {
		return future;
	}
	
	public IWorkable<V> getDangerousInnerClass(){
		return innerStep;
	}
	
	protected synchronized TaskQueue getQueue(){
		return this.queue;
	}

	protected synchronized void setQueue(TaskQueue queue){
		this.queue = queue;
	}


	@Override
	/**
	 * Attempts to start the step immediately. Returns false if no room is available in the task queue.
	 */
	public synchronized boolean start() {
		if(getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		if(queue == null)
			queue = TaskManager.queueWhereBlockingIsOk;
		return queue.offer(innerStep);
	}
	
	/**
	 * Attempts to start the step immediately. Returns false if no room is available in the task queue.
	 */
	public synchronized boolean start(TaskQueue queueToSubmitTo){
		if(getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		this.queue = queueToSubmitTo;
		return queueToSubmitTo.offer(innerStep);
	}
	/**
	 * Attempts to submit the step within the time allotted. Returns false if no room was available in the task queue.
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start(Long waitTime, TimeUnit unit) throws InterruptedException{
		if(getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		if(queue == null)
			queue = TaskManager.queueWhereBlockingIsOk;
		return queue.offer(innerStep, waitTime, unit);
	}
	
	/**
	 * Attempts to submit the step within the time allotted. Returns false if no room was available in the task queue.
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start(TaskQueue queueToSubmitTo, Long waitTime, TimeUnit unit) throws InterruptedException{
		if(getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		this.queue = queueToSubmitTo;
		return queueToSubmitTo.offer(innerStep, waitTime, unit);
	}
	
	/**
	 * InnerStep hides the doAction() method, which should only be called by the TaskQueue.
	 * @author Greg
	 *
	 * @param <V>
	 */
	private class InnerStep implements IWorkable<V>{
		
		private AtomicBoolean isStarted = new AtomicBoolean(false);
		private FutureResult<?,V> future;
		
		public InnerStep(FutureResult<?,V> future){
			this.future =future;
		}
		
		public synchronized boolean isStarted(){
			return isStarted.get();
		}
		
		public synchronized void setStarted(boolean isStartedVal){
			isStarted.set(isStartedVal);
		}
		
		public FutureResult<?,V> getFuture(){
			return future;
		}

		public synchronized Return<V> doAction()
				throws Exception {
				try{
					Return<V> toReturn = act();
					Status s = toReturn.getStatus();
					if(s == Status.COMPLETE)
						getFuture().setResult(toReturn.getReturn());
					return toReturn;
				}catch(Exception e){
					getFuture().setException(e);
				}
				//There must be an exception.
				return new Return<V>((V)null);
		}

		@Override
		public TaskQueue getTaskQueue() {
			return getQueue();
		}
		
		//TODO: Both this method and setQueue are synchronized. Is this a problem?
		public synchronized void setTaskQueue(TaskQueue queue){
			setQueue(queue);
		}

		@Override
		public void setException(Exception e) {
			getFuture().setException(e);
		}
		
	}
	
}
