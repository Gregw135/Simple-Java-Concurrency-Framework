package processingFrameworks.Concurrency;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import processingFrameworks.Concurrency.Support.FutureResult;

/**
 * An asynchronous executable process that can execute multiple steps, either
 * sequentially or in parallel. Task has 2 advantages over over a Runnable or
 * Callable: 1) processes don't have to block a thread while waiting for input, 
 * 2) it allows steps to be easily chained together.
 * 
 *@author Greg Womack
 * gregw134@gmail.com
 * 
 * @param <V>: Return type.
 * 
 */
public abstract class Task<V> implements ITask<V> {

	private final FutureResult<?, V> future;
	private final InnerTask innerTask;
	private volatile TaskQueue queue;

	public Task() {
		this.future = new FutureResult<Void, V>(null);
		this.innerTask = new InnerTask(future);
		this.queue = null;
	}

	/**
	 * Use this constructor if you want to pass in some value K to your
	 * futureResult. The FutureResult will take on type <K,V>.
	 * 
	 * @param <K>: Pass in some value K to be stored
	 * @param input
	 */
	public <K> Task(K input) {
		this.future = new FutureResult<K, V>(input);
		this.innerTask = new InnerTask(future);
		this.queue = null;
	}

	public Task(TaskQueue queue) {
		this.future = new FutureResult<Void, V>(null);
		this.innerTask = new InnerTask(future);
		this.queue = queue;
	}

	/**
	 * Use this constructor if you want to pass in some value K to your
	 * futureResult. The FutureResult will take on type <K,V>.
	 * 
	 * @param <K>: Pass in some value K to be stored
	 * @param input
	 */
	public <K> Task(K input, TaskQueue queue) {
		final FutureResult<K, V> f = new FutureResult<K, V>(input);
		this.future = f;
		this.innerTask = new InnerTask(future);
		/*
		 * innerTask.addTask(new Step<V>(){
		 * 
		 * @Override public FutureResult<K, V> getFuture() { return f; }
		 * 
		 * @Override public Return<V> act() throws Exception { Task.this.act();
		 * }
		 * 
		 * });
		 */
		this.queue = queue;
	}

	protected synchronized TaskQueue getQueue() {
		return queue;
	}

	@Override
	public FutureResult<?, V> getFuture() {
		return future;
	}

	/**
	 * The last step. This step will execute and return once all other steps
	 * have finished. Return a value V and a status indicating the progress of
	 * this step: RETRYNOW, RETRYLATER, COMPLETE.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract Return<V> act() throws Exception;

	// TODO: boolean continue on exception
	public <K> FutureResult<?, K> addStep(ITask<K> stepOrTask) {
		return innerTask.addTask(stepOrTask);
	}

	public FutureResult<Void, Void> addSteps(ITask... input) {
		return this.innerTask.addTasks(input);
	}

	public <K> FutureResult<?, K> addStep(boolean continueOnException,
			ITask<K> toAdd) {
		return innerTask.addTask(continueOnException, toAdd);
	}

	/**
	 * This task will attempt to submit a group of tasks which will run in
	 * parallel. The tasks might have to run sequentially, however, if no room
	 * is available to submit new tasks, so be careful not to submit tasks which
	 * wait on the others before continuing as this might cause deadlock.
	 * 
	 * TODO: Consider removing restrictions on submitting new tasks for child
	 * tasks. One approach is to create a new protected taskqueue method that
	 * reverses the process of offer, submitting a task before decrementing the
	 * semaphore. However this either requires a new kind of semaphore which
	 * supports decrementing without blocking, or downloading jdk 7 which has
	 * this method for semaphore.
	 * 
	 * @param continueOnException
	 * @param tasks
	 * @return
	 */
	public FutureResult<Void, Void> addSteps(boolean continueOnException,
			ITask... tasks) {
		return innerTask.addTasks(continueOnException, tasks);
	}

	@Override
	/**
	 * Attempts to submit the step. Returns false if no room was available in the task queue.
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start() {
		if (getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		if (queue == null)
			queue = TaskManager.queueWhereBlockingIsOk;
		return queue.offer(innerTask);
	}

	/**
	 * Attempts to submit the step. Returns false if no room was available in
	 * the task queue.
	 * 
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start(TaskQueue queueToSubmitTo) {
		if (getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		this.queue = queueToSubmitTo;
		return queueToSubmitTo.offer(innerTask);
	}

	/**
	 * Attempts to submit the step within the time allotted. Returns false if no
	 * room was available in the task queue.
	 * 
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start(Long waitTime, TimeUnit unit)
			throws InterruptedException {
		if (getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		if (queue == null)
			queue = TaskManager.queueWhereBlockingIsOk;
		return queue.offer(innerTask, waitTime, unit);
	}

	/**
	 * Attempts to submit the step within the time allotted. Returns false if no
	 * room was available in the task queue.
	 * 
	 * @param waitTime
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public synchronized boolean start(TaskQueue queueToSubmitTo, Long waitTime,
			TimeUnit unit) throws InterruptedException {
		if (getDangerousInnerClass().isStarted() == true)
			return true;
		getDangerousInnerClass().setStarted(true);
		this.queue = queueToSubmitTo;
		return queueToSubmitTo.offer(innerTask, waitTime, unit);
	}

	protected synchronized void setQueue(TaskQueue queue) {
		this.queue = queue;
	}

	/**
	 * Off-limits! This method should only be called by the Concurrency package.
	 */
	public IWorkable<V> getDangerousInnerClass() {
		return innerTask;
	}

	/**
	 * InnerTask hides the act() method, which should only be called by the
	 * TaskQueue.
	 * 
	 * @author Greg
	 * 
	 * @param <V>
	 */
	private class InnerTask implements IWorkableMultiple<V> {

		/**
		 * Design: Steps are processed sequentially, unless they are added
		 * together as a group, in which case they are processed in parallel.
		 * For example:
		 * 
		 * Step 1: Wait, then Step 2: Wait, then Step 3, 4, 5 (in parallel):
		 * wait, then:...
		 * 
		 * Remember that some steps might have sub-steps of their own. The V
		 * act() method of the Task class will always be the last step to be
		 * processed. By default, when a task encounters an exception, the
		 * exception will pass to the whole task and will be applied to all
		 * remaining steps. However, the task will be allowed to continue if a
		 * step was submitted with boolean continueOnException set to true.
		 */

		// Steps to go.
		private final ConcurrentLinkedQueue<InternalStep> remainingSteps;
		protected AtomicBoolean isStarted = new AtomicBoolean(false);
		private FutureResult<?, V> future;

		public InnerTask(FutureResult<?, V> future) {
			this.remainingSteps = new ConcurrentLinkedQueue<InternalStep>();
			this.future = future;
		}

		public FutureResult<?, V> getFuture() {
			return future;
		}

		public synchronized void setTaskQueue(TaskQueue queue) {
			setQueue(queue);
		}

		public boolean isStarted() {
			return isStarted.get();
		}

		public void setStarted(boolean isStartedVal) {
			isStarted.set(isStartedVal);
		}

		// TODO: add final step to queue. Set final step's future = future of
		// task.
		public synchronized Return<V> doAction() throws Exception {

			InternalStep task = remainingSteps.peek();
			if (task == null) {
				// Do last step.
				try {
					Return<V> toReturn = act();
					if (toReturn.getStatus() == Status.COMPLETE)
						getFuture().setResult(toReturn.getReturn());
					return toReturn;
				} catch (Exception e) {
					setException(e);
				}
			}

			// Do a step.
			try {
				Status s = task.act();
				if (s == Status.COMPLETE) {
					remainingSteps.remove(task);
					return new Return(Status.RETRYNOW);
				}
				return new Return(s);
			} catch (Exception e) {
				setException(e);
				throw e;
			}
		}

		public void setException(Exception e) {
			while (true) {
				InternalStep s = remainingSteps.poll();
				if (s == null)
					return;
				s.setException(e);
				getFuture().setException(e);
			}
		}

		@Override
		public synchronized <K> FutureResult<?, K> addTask(ITask<K> toAdd) {
			this.remainingSteps.add(new SingleStep(toAdd, false));
			return toAdd.getFuture();
		}

		public synchronized <K> FutureResult<?, K> addTask(
				boolean continueOnException, ITask<K> toAdd) {
			this.remainingSteps.add(new SingleStep(toAdd, continueOnException));
			return toAdd.getFuture();
		}

		public synchronized FutureResult<Void, Void> addTasks(ITask... tasks) {
			FutureResult<Void, Void> toReturn = new FutureResult<Void, Void>(
					null);
			this.remainingSteps.add(new MultipleStep(false, toReturn, tasks));
			return toReturn;
		}

		public synchronized FutureResult<Void, Void> addTasks(
				boolean continueOnException, ITask... tasks) {
			FutureResult<Void, Void> toReturn = new FutureResult<Void, Void>(
					null);
			this.remainingSteps.add(new MultipleStep(continueOnException,
					toReturn, tasks));
			return toReturn;
		}

		@Override
		public TaskQueue getTaskQueue() {
			return getQueue();
		}

	}

	interface InternalStep {

		public Status act() throws Exception;

		public void setException(Exception e);

	}

	/**
	 * Contains either a single step/task or a collection of steps/tasks that
	 * will run in parallel. TODO: Add methods here.
	 * 
	 * @author Greg
	 * 
	 */
	private class SingleStep implements InternalStep {

		private final ITask task;
		private final boolean continueOnException;

		public SingleStep(ITask task, boolean continueOnException) {
			this.continueOnException = continueOnException;
			this.task = task;
		}

		@Override
		public Status act() throws Exception {
			try {

				Return toReturn = task.getDangerousInnerClass().doAction();
				if (toReturn.getStatus() == Status.COMPLETE) {
					task.getFuture().setResult(toReturn.getReturn());
					return Status.COMPLETE;
				} else {
					return toReturn.getStatus();
				}
			} catch (Exception e) {
				task.getFuture().setException(e);
				if (continueOnException == false) {
					throw e;
				} else {
					return Status.COMPLETE;
				}
			}
		}

		@Override
		public void setException(Exception e) {
			task.getDangerousInnerClass().setException(e);
		}

	}

	/**
	 * Tries to execute multiple steps in parallel. If there isn't enough room
	 * available in the TaskQueue for new tasks they will be executed
	 * sequentially in this task.
	 * 
	 * @author Greg
	 * 
	 */
	private class MultipleStep implements InternalStep {

		private final HashSet<ITask> taskSet;
		private final boolean continueOnException;
		private final AtomicBoolean allTasksStarted = new AtomicBoolean(false);
		private final FutureResult<Void, Void> future;

		public MultipleStep(boolean continueOnException,
				FutureResult<Void, Void> future, ITask... tasks) {
			this.continueOnException = continueOnException;
			taskSet = new HashSet<ITask>();
			for (ITask task : tasks)
				taskSet.add(task);
			this.future = future;
		}

		@Override
		public Status act() throws Exception {
			if (allTasksStarted.get() == false) {
				Iterator<ITask> itr = taskSet.iterator();
				while (itr.hasNext()) {
					ITask task = itr.next();
					if (task.getDangerousInnerClass().isStarted() == true)
						continue;
					TaskQueue queue = task.getDangerousInnerClass()
							.getTaskQueue();
					// If no queue is specified, use current task queue.
					if (queue == null) {
						queue = getQueue();
						task.getDangerousInnerClass().setTaskQueue(queue);
					}
					// Start this as a new task if there is room available.
					// Otherwise, execute the task in this thread.
					boolean wasAdded = task.start();
					if (wasAdded == true) {
						continue;
					} else {
						// The TaskQueue has no room to add new tasks.
						// Execute the task in this thread.
						try {
							Status s = task.getDangerousInnerClass().doAction()
									.getStatus();
							if (s == Status.COMPLETE)
								return Status.RETRYNOW;
							return s;
						} catch (Exception e) {
							task.getFuture().setException(e);
							if (continueOnException == false) {
								Iterator<ITask> itr2 = taskSet.iterator();
								setException(e);
							}
						}
					}
					continue;
				}
				allTasksStarted.set(true);
			}

			// All tasks should be started.

			// Check if all tasks have successfully completed.
			Iterator<ITask> itr = taskSet.iterator();
			while (itr.hasNext()) {
				ITask task = itr.next();
				if (task.getFuture().isDone() == false)
					return Status.RETRYLATER;
				if (continueOnException == false
						&& task.getFuture().hasException() == true) {
					Iterator<ITask> itr2 = taskSet.iterator();
					Exception e1 = task.getFuture().getException();
					setException(e1);
					throw e1;
				}
			}
			// All tasks have successfully completed (or we continued after
			// exceptions).
			future.setResult(null); // release future
			return Status.COMPLETE;
		}

		@Override
		public void setException(Exception e) {
			Iterator<ITask> itr2 = taskSet.iterator();
			while (itr2.hasNext()) {
				itr2.next().getDangerousInnerClass().setException(e);
			}
			future.setException(e);
		}

	}

}
