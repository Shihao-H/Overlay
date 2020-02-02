package cs455.overlay.transport;

import cs455.overlay.node.*;
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.lang.Runnable;

/*Listening to certain sockets, for instance, sockets accepted by server threads, 
and client side sockets also need a receiver thread.*/
public class TCPReceiverThread implements Runnable
{
    private Socket socket;
    private DataInputStream din;
    private Node node;
    public TCPReceiverThread(Socket socket, Node node) throws IOException
    {
        this.socket = socket;
        this.node = node;
        din = new DataInputStream(this.socket.getInputStream());
    }

    public void run()
    {
        int dataLength;
        while (this.socket != null)
        {
            try
            {
                dataLength = din.readInt();
                byte[] marshalledBytes = new byte[dataLength];
                din.readFully(marshalledBytes, 0, dataLength);
                EventFactory factory = EventFactory.getInstance();
                Event event = factory.generateEvent(marshalledBytes);
                node.onEvent(event, socket);
            } 
            catch (SocketException se)
            {
                System.out.println(se.getMessage());
                break;
            } 
            catch (IOException ioe)
            {
                System.out.println(ioe.getMessage()) ;
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}