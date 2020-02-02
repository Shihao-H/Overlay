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

/*Propogate the whole overlay info, basically nodes info to every messaging node*/
public class InformNodesOverlay implements Event, Protocol
{
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String wholeOverlay = new String();
    /*This constrcutor is used  for sending*/
    public InformNodesOverlay(String wholeOverlay, int type)
    {
        this.wholeOverlay = wholeOverlay;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public InformNodesOverlay(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        byte[] temp = wholeOverlay.getBytes();
        dout.writeInt(Protocol.INFORM__NODES_OVERLAY);
        dout.writeInt(temp.length);
        dout.write(temp);

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
        wholeOverlay = new String(identifierBytes);

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public String getInfo()
    {
        return this.wholeOverlay;
    }

}