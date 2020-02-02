package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/*Notify all the messaging nodes to send their Tracker(statistics) information*/
public class PullTrafficSummary implements Event, Protocol
{
    private int type = -1;
    private byte[] marshalledBytes = new byte[0];
    /*This constrcutor is used  for sending*/
    public PullTrafficSummary(int type)
    {
        this.type = type;
    }

    /*This constrcutor is used for decoding*/
    public PullTrafficSummary(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.PULL_TRAFFIC_SUMMARY);

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

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

}