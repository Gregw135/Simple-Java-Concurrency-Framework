package processingFrameworks.Concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import processingFrameworks.Concurrency.Support.CustomThreadFactory;

/**
 * A place to send tasks until they're processed. A task queue doubles as an executor service;
 * it creates its own threads to process tasks.
 * (Code seems good).
 *@author Greg Womack
 * gregw134@gmail.com
 *
 */
public class TaskQueue {
	
	/**
	 * Design: A set of workers operate on a set of tasks, which may contain multiple steps. 
	 * A worker will perform steps of the task until either the task is complete, or the task 
	 * signals that it needs to wait for input. Tasks waiting for input are moved to the back 
	 * of the work queue; tasks that are compete are removed and room is made available for another
	 * task to take its place.
	 */
	
	private static final int TASK_QUEUE_MAX_SIZE = 100000;

	private Semaphore taskCount;
	private LinkedBlockingQueue<IWorkable> tasks = new LinkedBlockingQueue<IWorkable>();
	
	private ExecutorService exec;
	
	public TaskQueue(){
		this.taskCount = new Semaphore(TASK_QUEUE_MAX_SIZE);
		this.exec = Executors.newCachedThreadPool(new CustomThreadFactory("Task Worker", 5));
		initialize(exec, 1, TASK_QUEUE_MAX_SIZE);
	}
	
	public TaskQueue(int threadCount, int priority){
		this.taskCount = new Semaphore(TASK_QUEUE_MAX_SIZE);
		this.exec = Executors.newCachedThreadPool(new CustomThreadFactory("Task Worker", priority));
		initialize(exec, threadCount, TASK_QUEUE_MAX_SIZE);
	}
	
	private void initialize(ExecutorService service, int threadCount, int maxTaskCount){
		for(int i = 0; i< threadCount; i++){
			exec.submit(new StepWorker(this)); //TODO: THIS reference shouldn't escape during construction?
		}		
	}
	
	/**
	 * Returns immediately. Null is returned if the queue is empty.
	 * @return
	 */
	public IWorkable poll(){
		return tasks.poll();
	}
	
	/**
	 * Step Worker only: Return a task for later computation. Shouldn't block. Returns true or throws an exception.
	 * @param step
	 * @return
	 * @throws IllegalStateException
	 */
	protected boolean returnTask(IWorkable step) throws IllegalStateException{
		return tasks.add(step);
	}
	
	/**
	 * Step Worker only: Inform the task queue that a task is completed. Releases space for new tasks.
	 * @param step
	 */
	protected void releaseTask(IWorkable step){
		taskCount.release();
	}

	
	public IWorkable poll(Long timeout, TimeUnit unit) throws InterruptedException{
		return tasks.poll(timeout, unit);
	}
	
	public void add(IWorkable step) throws IllegalStateException{
		boolean success = taskCount.tryAcquire();
		if(success){
			try{
				tasks.add(step);
			}catch(IllegalStateException e){
				taskCount.release();
				throw e;
			}
		}else{
			throw new IllegalStateException();
		}
	}
	/**
	 * Inserts the specified element at the tail of this queue if it is possible to do so immediately without exceeding the queue's capacity, returning true upon success and false if this queue is full.
	 * @param step
	 * @return
	 */
	public boolean offer(IWorkable step){
		boolean success = taskCount.tryAcquire();
		if(success){
			boolean added = tasks.offer(step);
			if(added == false)
				taskCount.release();
			return added;
		}else{
			return false;
		}
	}
	
	public boolean offer(IWorkable step, Long timeout, TimeUnit unit) throws InterruptedException{
		boolean success = taskCount.tryAcquire(timeout, unit);
		if(success){
				boolean added = tasks.offer(step, timeout, unit);
				if(added == false)
					taskCount.release();
				return added;
		}else{
			return false;
		}
	}
}
