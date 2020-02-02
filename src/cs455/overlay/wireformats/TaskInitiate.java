package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*Tell mesaging nodes to start rounding*/
public class TaskInitiate implements Event, Protocol
{
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private int rounds = -1;
    /*This constrcutor is used  for sending*/
    public TaskInitiate(int rounds, int type)
    {
        this.rounds = rounds;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public TaskInitiate(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.TASK_INITIATE);
        dout.writeInt(rounds);

        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }    

    public void unMarshall() throws IOException
    {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));

        this.type = din.readInt();
        rounds = din.readInt();

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public int getRounds()
    {
        return this.rounds;
    }

}