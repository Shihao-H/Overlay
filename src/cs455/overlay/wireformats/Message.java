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

/*This class is used for wraping round messages, it contains a payload, a String of rounting path, and a counter for tracking how many nodes left. */
public class Message implements Event, Protocol
{
    private int payload = 0;
    private int remain = -1;
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String rountingCache = new String();
    private String original = new String();
    /*This constrcutor is used  for sending*/
    public Message(int payload, String rountingCache, String original, int remain, int type)
    {
        this.payload = payload;
        this.rountingCache = rountingCache;
        this.original = original;
        this.remain = remain;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public Message(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.MESSAGE);
        dout.writeInt(this.payload);

        byte[] rountingCacheBytes = this.rountingCache.getBytes();
        dout.writeInt(rountingCacheBytes.length);
        dout.write(rountingCacheBytes);

        byte[] originalBytes = this.original.getBytes();
        dout.writeInt(originalBytes.length);
        dout.write(originalBytes);

        dout.writeInt(this.remain);
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
        this.payload = din.readInt();
        int rountingCacheLength = din.readInt();
        byte[] rountingCacheBytes = new byte[rountingCacheLength];
        din.readFully(rountingCacheBytes);
        this.rountingCache = new String(rountingCacheBytes);

        int originalLength = din.readInt();
        byte[] originalBytes = new byte[originalLength];
        din.readFully(originalBytes);
        this.original = new String(originalBytes);

        this.remain = din.readInt();
        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public int getRemain()
    {
        return this.remain;
    }

    public int getPayload()
    {
        return this.payload;
    }

    public String getRountingCache()
    {
        return this.rountingCache;
    }

    public String getOriginal()
    {
        return this.original;
    }
}