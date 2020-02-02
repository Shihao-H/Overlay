package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*Wraping the necessary RegisterResponse/deRegisterResponse information.*/
public class RegisterResponse implements Event, Protocol
{
    private String info = "";
    private byte status = 0;
    private int type = -1;
    private byte[] marshalledBytes = new byte[0];

    /*This constrcutor is used  for sending, type 1 means RegisterResponse, type 2 means deRegisterResponse*/
    public RegisterResponse(String info, byte status, int type)
    {
        this.info = info;
        this.status = status;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public RegisterResponse(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        dout.writeInt(Protocol.REGISTER_RESPONSE);

        if(status == 100)
            dout.writeByte(Protocol.SUCCESS);
        else
            dout.writeByte(Protocol.FAILURE);

        byte[] infoStr = info.getBytes();
        dout.writeInt(infoStr.length);
        dout.write(infoStr);
        dout.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dout.close();
        return marshalledBytes;
    }

    @Override
    public void unMarshall() throws IOException
    {
        ByteArrayInputStream baInputStream =
        new ByteArrayInputStream(marshalledBytes);
        DataInputStream din =
        new DataInputStream(new BufferedInputStream(baInputStream));
        this.type = din.readInt();
        this.status = din.readByte();
        int identifierLength = din.readInt();
        byte[] identifierBytes = new byte[identifierLength];
        din.readFully(identifierBytes);
        this.info = new String(identifierBytes);
        baInputStream.close();
        din.close();
    }

    @Override
    public int getType() {
        return type;
    }

    public String getInfo()
    {
        String statusString = "";
        String protocol = "";
        if(type==Protocol.REGISTER_RESPONSE)
            protocol = new String("REGISTER_RESPONSE");

        if(type==Protocol.DEREGISTER_RESPONSE)
            protocol = new String("DEREGISTER_RESPONSE");

        if(this.status==Protocol.SUCCESS)
            statusString = new String("SUCCESS");
            
        if(this.status==Protocol.FAILURE)
            statusString = new String("FAILURE");

        return new String(protocol + " " + statusString + " " + info);
    }

    

}