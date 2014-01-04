package processingFrameworks.Concurrency;


/**
 * (This class should remain private to this package). 
 * A class wrapper for the enum Status.
 * @author Greg Womack
 * gregw134@gmail.com
 *
 */
public class StatusSignal{
	private Status status;
	public StatusSignal(Status status){
		this.status = status;
	}
	
	public Status getStatus(){
		return status;
	}
}
