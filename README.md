Simple-Java-Concurrency-Framework
=================================

A human-friendly framework for concurrent programming. Allows concurrent programs to be written using familiar concepts like Step and Task, instead of computer-centric terms like Thread, Fork/join, etc.  

This is a new project, so all commments, suggestions and criticisms will be welcomed.


**Composing tasks:**

Task makeBreakfast = new BreakfastTask();  
mainTask.addStep(new PutBreadInToaster());  
mainTask.addStep(new StartToaster());  
mainTask.addSteps(new GetOJ(), new CookEggs(), new CookBacon()); //These steps will run concurrently once the toast is                                                                 //started                                                                        
Future<Breakfast> futureBreakfast = mainTask.addStep(new AssembleBreakfast()); //Starts once the previous steps have finished.   makeBreakfast.start();                                                        
Breakfast b = futureBreakfast.get();    



**Wait without threads:**

Task WaitThenPrint = new Task<String>(){  
  
   final long time1 = System.currentTimeMillis();  
   public Return<String> act(){  
     if((System.currentTimeMillis() - time1) < 1000){  
       //Task is placed at the end of a task queue, and will be called again later.  
       //The thread is released to work on something else.  
       return new Return<String>(Status.RetryLater);  
     }else{  
       System.out.println("The brown fox jumped over the lazy dog.");  
       return new Return<String>("Some return value");  
      }  
   }  
};  


Every step returns a Future class that allows its progress to be monitored:  

FutureResult<InputType,ResultType> f = someTask.getFuture();  

FutureResult allows results and exceptions to be set directly:  
f.setResult(someResult);  
f.setException(someException);  

Completion events can be registered:
f.addEvent(new BeforeReturnEvent<InputType,ResultType>(){  
    public void doBeforeReturn(FutureResult<InputType,ReturnType>, V dataToReturn){  
          System.out.println("Returned: " + dataToReturn.toString());  
    }  
}  


  
See Example.java for a more thorough demonstration of the framework.  

