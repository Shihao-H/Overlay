package cs455.overlay.transport;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.Runnable;

/*Each nodes and registry will have a sender thread keep trying to accquire tasks from the queue.*/
public class TCPSenderThread implements Runnable
{
    private LinkedBlockingQueue<Task> queue;
    public TCPSenderThread(LinkedBlockingQueue<Task> queue) throws IOException
    {
        this.queue = queue;
    }

    public void run()
    {
    	Task task;
    	while(true)
    	{
    		try
    		{
				//retrieve and remove the head of this queue. 
				//If the queue is empty then it will wait until an element becomes available. 
				task = queue.take();
				try
				{
					task.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
    		catch (InterruptedException e)
    		{
				break;
			}
    		
    	}
    }
}