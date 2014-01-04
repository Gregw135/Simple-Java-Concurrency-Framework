package processingFrameworks.Concurrency.Example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import processingFrameworks.Concurrency.Return;
import processingFrameworks.Concurrency.Status;
import processingFrameworks.Concurrency.Step;
import processingFrameworks.Concurrency.Task;


/**
 * A demonstration of the Task class. 
 * Example 1 shows that Task can outperform Java's ExecutorService in some situations. 
 * The reason is that Task makes it easy to pause a process release its thread to work on another task.
 * 
 * Example 2 shows how a Task can be composed of subtasks, and how subtasks 
 * can be added in groups that run concurrently. This can allow for easier design of program flow.
 */
public class Example {

	public static void main(String[] args){
		try{
			System.out.println("Performance Example. We're going to run 10000 tasks concurrently, \n " +
					"each of waits 1000 milliseconds before printing a value.");
			Thread.currentThread().sleep(3000);
			performanceExample();
			
			Thread.currentThread().sleep(3000);
			System.out.println("\n Example: Composing tasks using subtasks.");
			Thread.currentThread().sleep(2000);
			
			processOrderingExample();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Used in example 1.
	 * Long: elapsed time.
	 */
	private static class WaitThenPrint extends Step<Long>{

		private final String toPrint;
		private final int timeToWait;
		volatile long timeWhenWaitStarted;
		
		public WaitThenPrint(String toPrint, int timeToWait){
			this.toPrint = toPrint;
			this.timeToWait = timeToWait;
			timeWhenWaitStarted = System.currentTimeMillis();
		}
	
		protected Return<Long> act() throws Exception {
			if(System.currentTimeMillis() - timeWhenWaitStarted > timeToWait){
				long waitTime =  (System.currentTimeMillis() - timeWhenWaitStarted);
				System.out.println(toPrint);
				return new Return<Long>(waitTime);
			}
			//Send task to the end of the task queue, and release thread to work on some other task.
			return new Return<Long>(Status.RETRYLATER);
		}
		
	}
	
	/**
	 * Executes a large number of concurrent processes using the Task class. 
	 * This is shown to be faster than submitting processes in an ExecutorService.
	 * 
	 */
	public static void performanceExample(){
		
		Future<Long>[] results = new Future[10000];
		long beginningTime = System.currentTimeMillis() + 1000; //(Tasks wait 1000 before starting).
		try{
			/*
			 * Submit tasks.
			 */
			
			for(int i = 0; i < 10000; i++){
				WaitThenPrint print1 = new WaitThenPrint("Task " +i + " completed" , 1000);
				print1.start();
				results[i] = print1.getFuture();
			}
			/*
			 * Wait until tasks are done.
			 */
			for(int i = 0; i< 10000; i++){
				Future<Long> waitingComputation = results[i];
				//wait until done.
					waitingComputation.get();
			}
			
			long timeRequired = System.currentTimeMillis() - beginningTime;
			System.out.println("Time required to process 10000 long-running tasks: " + timeRequired + " milliseconds.");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		/*
		 * For comparison, let's try doing this again by submitting Runnables to an Executor.
		 */
		try{
			Thread.currentThread().sleep(3000);
			System.out.println("Let's compare this to a Java ExecutorService.");
			Thread.currentThread().sleep(8000);
		
		
		final ExecutorService exec = Executors.newCachedThreadPool(); //.newFixedThreadPool(4);
		Future[] results2 = new Future[10000];
		
		final long time1 = System.currentTimeMillis();
		for(int i =0; i< 10000; i++){
			final int threadNum = i;
			results2[i] = exec.submit(new Runnable(){
			
				public void run(){
					long initialTime = System.currentTimeMillis();
					while((System.currentTimeMillis() - initialTime) < 1000){
						Thread.currentThread().yield();
						
						/*if((System.currentTimeMillis() - time1) > 10000)
							exec.shutdownNow();*/
					}
					System.out.println("Task " + threadNum + ". Time waited: " + (System.currentTimeMillis() - time1) + " milliseconds.");
				}
			});
		}
		
		/*
		 * Wait until tasks are done.
		 */
		for(int i = 0; i< 10000; i++){
			Future waitingComputation = results2[i];
			//wait until done.
				waitingComputation.get();
		}
		
		System.out.println("Time required to process 10000 tasks using an ExecutorService: " + (System.currentTimeMillis() - time1));
		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
		/**
		 * Tasks can be composed of subtasks. 
		 */
		public static void processOrderingExample(){
			
			try{
			
			final Task rootTask = new Task<Void>(){
				@Override
				protected Return<Void> act() throws Exception {
					//Any action placed here will execute after all subtasks have completed.
					return new Return<Void>(Status.COMPLETE);
				}
				
			};
			
			/**
			 * Add step1.
			 */
			rootTask.addStep(new Task<Void>(){
				@Override
				protected Return<Void> act() throws Exception {
					System.out.println("10 Brown Foxes jumped over the lazy dog. They landed in this order: \n");
					Thread.currentThread().sleep(3000);
					//If this Task has more subtasks, proceed immediately to the next one. Otherwise, allow the thread 
					//to execute some other task.
					return new Return<Void>(Status.COMPLETE);
				}
			});
			
			/**
			 * These steps will execute simultaneously as soon as step1 completes.
			 */			
			Task[] tenFoxesJumping = new Task[10];
			for(int i = 0; i< 10; i++){
				final int j = i;
				tenFoxesJumping[i] = new Task<Void>(){
					@Override
					protected Return<Void> act() throws Exception {
						System.out.println("Fox " + j + " ");
						return new Return<Void>(Status.COMPLETE);
					}					
				};
			}
			//Add steps which will execute concurrently.
			rootTask.addSteps(tenFoxesJumping);
			
			//You can also add new Tasks using the Step class, which is smaller and runs faster.
			//However, you can't add subtasks to a step class.
			rootTask.addStep(new Step<Void>(){

				@Override
				protected Return<Void> act() throws Exception {
					Thread.currentThread().sleep(2000);
					System.out.println("The lazy dog woke up, and was not amused.");
					return new Return<Void>(Status.COMPLETE);
				}
					
			});
			
			rootTask.start();
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			
		}
		
		
			
			/*
			
		
		
		
		Task<Integer> basicTask = new Task<Integer>(){
			volatile int count = 0;
			@Override
			protected Return<Integer> act() throws Exception {
				
				if(count < 3){
					count++;
					System.out.println("count = " + count + ". Step will be sent to the end of the task queue.");
					return new Return<Integer>(Status.RETRYLATER);
				}else{
					System.out.println("count = " + count + ". Step will terminate and return.");
					return new Return<Integer>(count);
				}
				
			}
		};
		
		FutureResult<?,Integer> future = basicTask.getFuture();
		basicTask.start();
		
		try{
			System.out.println(future.get());
		}catch(Exception e){
			e.printStackTrace();
		}
	*/	

	
	
}
