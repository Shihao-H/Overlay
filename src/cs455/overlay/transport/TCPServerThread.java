package cs455.overlay.transport;

import cs455.overlay.node.*;
import java.io.IOException;
import java.net.*;
import java.lang.Runnable;

/*This class is used to form a Serversocket and listening all incoming connections. 
Each time a connection is accecpted, a new receiver thread will be make and associate with it.*/
public class TCPServerThread implements Runnable
{
    private final ServerSocket ServerSocket;
    private final int NUM_POSSIBLE_CONNECTIONS;
    private final int OUR_PORT;
    private final Node node;
    public Socket incomingConnectionSocket;
    public TCPServerThread(int numberCon, int port, Node node) throws IOException
    {
        this.NUM_POSSIBLE_CONNECTIONS = numberCon;
        this.OUR_PORT = port;
        this.node = node;
        this.ServerSocket = new ServerSocket(OUR_PORT, NUM_POSSIBLE_CONNECTIONS);
    }

    public void run()
    {
        try
        {
            while(ServerSocket!=null)
            {
                incomingConnectionSocket = this.ServerSocket.accept();
                // If we get here we are no longer blocking, so we accepted a new connection
                System.out.println("We received a connection!");
                (new Thread(new TCPReceiverThread(incomingConnectionSocket, node))).start();
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}


