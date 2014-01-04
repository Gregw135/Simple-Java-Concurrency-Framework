package processingFrameworks.Concurrency;

import java.util.concurrent.TimeUnit;


/**
 * A worker that cycles through tasks and performs the ones that are ready.  
 * (Step Worker also sets the return or exception values of a task.) 
 * @author Greg Womack
 * gregw134@gmail.com
 * 
 * TODO: Provide your own shutdown handler? If IWorkable classes swallow interrupts, StepWorker
 * threads might not shutdown.
 *
 */
public class StepWorker implements Runnable{

private TaskQueue input;
	
	public StepWorker(TaskQueue input){
		this.input = input;
	}
	
	
	public void run(){
		IWorkable task = null;
		while(true){
			if(Thread.currentThread().isInterrupted()){
				System.out.println("Worker thread " + Thread.currentThread().getName() + " was interrupted and is exiting.");
				return;
			}
			try {
				task = input.poll(new Long(1000), TimeUnit.MILLISECONDS);
				if(task == null)
					continue;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				continue;
			}
			
			Status doAgain = performActionAndReturnResults(task);

			
			while(doAgain == Status.RETRYNOW){
				doAgain = performActionAndReturnResults(task);
			}
		}
	}
	
	/**
	 * Performs a task's action and decides what to do with the results. If the task signals retry now, it passes that signal back.
	 * If the task signals retry later it returns the task to the work queue. If the task returns a result or an exception it 
	 * passes the data back to the task's future.
	 * @param step
	 * @return
	 * @throws Exception
	 */
	private Status performActionAndReturnResults(IWorkable task){
		try{
			StatusSignal status = task.doAction();
			if(status.getStatus() == Status.RETRYLATER){
				input.returnTask(task);
				return Status.RETRYLATER;
			}else if(status.getStatus() == Status.COMPLETE){
				input.releaseTask(task);
				return Status.COMPLETE;
			}else if(status.getStatus() == Status.RETRYNOW){
				return Status.RETRYNOW;
			}else{
				throw new RuntimeException("Unknown Status.");
			}
			//return status.getStatus();
		}catch(Exception e){
			e.printStackTrace();
			if(e instanceof InterruptedException)
				Thread.currentThread().interrupt();
			task.setException(e);
			input.releaseTask(task);
			return Status.COMPLETE;
		}
	}
	
}
