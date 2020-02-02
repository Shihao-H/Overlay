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

import cs455.overlay.node.Tracker;

/*Notify registry that one rounding message task is complete, it has the String for storing the original node address, 
so the registry can notify the node that this message task is complete*/
public class NodeTaskComplete implements Event, Protocol
{
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String original = new String();
    /*This constrcutor is used  for sending*/
    public NodeTaskComplete(String original, int type)
    {
        this.original = original;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public NodeTaskComplete(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.NODE_TASK_COMPLETE);

        byte[] original_info = this.original.getBytes();
        dout.writeInt(original_info.length);
        dout.write(original_info);
        
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
        
        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        this.original = new String(identifierBytes);

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public String getOriginal() { 
        return this.original;
    } 
}