Simple-Java-Concurrency-Framework
=================================

A human-friendly framework for concurrent programming. Allows concurrent programs to be written using familiar concepts like Step and Task, instead of computer-centric terms like Thread, Fork/join, etc.

Examples:

Task composition:

Task<Breakfast> makeBreakfast = new BreakfastTask();  
mainTask.addStep(new GetBread());  
mainTask.addStep(new PutBreakInToaster());  
mainTask.addStep(new StartToaster());  
mainTask.addSteps(new GetOJ(), new CookEggs(), new CookBacon()); //These steps will run concurrently once the toast is                                                                          //started.    
Future<Breakfast> futureBreakfast = mainTask.addStep(new AssembleBreakfast()); //Starts once the previous steps have  makeBreakfast.start();                                                        //all finished.  
Breakfast b = futureBreakfast.get();    



Efficient waiting:

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

  
See Example.java for a more thorough demonstration of the framework.  

