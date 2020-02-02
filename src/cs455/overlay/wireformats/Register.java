package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*Wraping the necessary registering/deregistering information.*/
public class Register implements Event, Protocol
{
    private String ip = "";
    private int port = 0;
    private int type = -1;
    private byte[] marshalledBytes = new byte[0];


    /*This constrcutor is used  for sending*/
    public Register(String ip, int port, int type)
    {
        this.ip = ip;
        this.port = port;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public Register(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        byte[] identifierBytes = ip.getBytes();

        if(type==0)
            dout.writeInt(Protocol.REGISTER_REQUEST);
        else
            dout.writeInt(Protocol.DEREGISTER_REQUEST);

        dout.writeInt(identifierBytes.length);
        dout.write(identifierBytes);
        dout.writeInt(port);
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
        this.ip = new String(identifierBytes);
        this.port = din.readInt();
        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public String getIpAddress()
    {
        return new String(ip+": "+Integer.toString(port));
    }
}