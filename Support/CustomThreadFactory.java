package processingFrameworks.Concurrency.Support;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

/**
 * Assigns a name, priority and uncaughtExceptionHandler to created threads.
 * @author Greg
 *
 */
public class CustomThreadFactory implements ThreadFactory{

	String name;
	int priority;
	private int i = 0;
	
	public CustomThreadFactory(String name, int threadPriority){
		this.name = name;
		this.priority = threadPriority;
	}	
		
		@Override
		public Thread newThread(Runnable arg0) {
			Thread t = new Thread(arg0);
			t.setPriority(priority);
			t.setName(name + i);
			i++;
			t.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){
				public void uncaughtException(Thread t, Throwable e){
					System.err.println("\n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n  Thread died!" +
							" \n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n");
					if(e instanceof Exception){
						//ErrorLogger.reportError((Exception) e, "\n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n  Thread died!" +
						//" \n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n");
					}else{
						/*ErrorLogger.reportError("\n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n  Thread died!" +
								" \n !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n"
								+ e.toString());*/
					}
					e.printStackTrace();
				}
			});
			return t;
		}
}
