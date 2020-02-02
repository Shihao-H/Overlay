package cs455.overlay.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*The tasks will be stored in the queue and sender thread will keep fetching it, 
it has the bytes encoded by the Events and the socket infos needed by the sender thread.*/
public class Task
{
    private Socket socket;
    private byte[] bytes; 
    public Task(Socket socket, byte[] bytes)
    {
        if(socket==null)
        {
            throw new NullPointerException("Task"); 
        }
        this.socket = socket;
        this.bytes = bytes;
    }

    public Socket getSocket()
    {
        return socket;
    }

    public byte[] getBytes()
    {
        return bytes;
    }

    public void execute() throws IOException
    {
        DataOutputStream dout = new DataOutputStream(this.socket.getOutputStream());
        int dataLength = this.bytes.length;
        try {
            dout.writeInt(dataLength);
            dout.write(this.bytes, 0, dataLength);
            dout.flush();
        } catch (IOException e)
         {
            e.printStackTrace();
        }
    }
}