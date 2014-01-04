package processingFrameworks.Concurrency;




/**
 * Submit tasks here. 
 * 
 * Design note: I've decided to have two queues here, one for blocking and one for fast computations. 
 * blocking tasks
 * 
 *@author Greg Womack
 * gregw134@gmail.com
 * 11/2/2013
 */
public class TaskManager {
	
	private static final int 	THREAD_COUNT_FOR_BLOCKING_TASK_QUEUE = 4;
	private static final int 	THREAD_COUNT_FOR_NONBLOCKING_TASK_QUEUE = 4;
	
	private static final int PRIORITY_FOR_BLOCKING_TASK_QUEUE = 5;
	private static final int PRIORITY_FOR_NONBLOCKING_TASK_QUEUE = 9;
	
	public static TaskQueue queueWhereBlockingIsOk = new TaskQueue(THREAD_COUNT_FOR_BLOCKING_TASK_QUEUE, PRIORITY_FOR_BLOCKING_TASK_QUEUE);
	public static TaskQueue fastComputationOnlyQueue = new TaskQueue(THREAD_COUNT_FOR_NONBLOCKING_TASK_QUEUE, PRIORITY_FOR_NONBLOCKING_TASK_QUEUE);
}
