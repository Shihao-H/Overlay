package cs455.overlay.transport;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

/*ConnectionManager has two data structrues. A linkedBlockingQueue can gurantee 
the safe operation under multi-thread condition. the hashtable is used to store 
the nodes and their corresponding sockkets*/

public class ConnectionManager {
    private LinkedBlockingQueue<Task> queue;
    private Hashtable<String, Socket> socketMapper;

    public ConnectionManager() {
        this.queue = new LinkedBlockingQueue<Task>();
        this.socketMapper = new Hashtable<String, Socket>();
    }
    
    /*Inserts the specified element at the tail of this queue, waiting if necessary for space to become available.*/
    public void addTask(Task task) throws InterruptedException {
        this.queue.put(task);
    }

    public void addUnit(String key, Socket value) {
        this.socketMapper.putIfAbsent(key, value);
    }

    public void removeUnit(String key) throws IOException
    {
        this.socketMapper.get(key).close();
        this.socketMapper.remove(key);
    }

    public LinkedBlockingQueue<Task> getQueue()
    {
        return this.queue;
    }

    public Socket getSocket(String key)
    {
        return this.socketMapper.get(key);
    }

    public Hashtable<String, Socket> getHashtable()
    {
        return this.socketMapper;
    }
}