package processingFrameworks.Concurrency;


public class Return<V> extends StatusSignal{
	
	private Status status;
	private V toReturn;
	

	/**
	 * Send a RETRYNOW or RETRYLATER signal to the Task Queue. 
	 * @param status
	 * @author Greg Womack
	 * gregw134@gmail.com
	 */
	
	public Return(Status status){
		super(status);
		this.status = status;
		//if(status == Status.COMPLETE)
			//throw new RuntimeException("You can't set Status.RETURN without providing a return value.");
		this.toReturn = null;
	}
	
	/**
	 * Using this constructor will cause this step to end and data to be returned.
	 * @param returnContent
	 */
	public Return(V returnContent){
		super(Status.COMPLETE);
		this.status = Status.COMPLETE;
		this.toReturn = returnContent;
	}
	
/*
	public Return(Status status, V toReturn){
		super(status);
		this.status= status;
		this.toReturn = toReturn;
	}
*/		
	
	@Override
	public Status getStatus(){
		return status;
	}
	
	public V getReturn(){
		return toReturn;
	}
}