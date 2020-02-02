package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/*Every time we muse a client side socket to connect to the ServerSocket of another node, machine will automically assign a port to it,
we need to notify the node we connected to the socket info of our currentr node, so it can add it to its connectionManager hashtable*/
public class NotifySocketInfo implements Event, Protocol
{
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String socketInfo = new String();
    private String oldLabel = new String();
    private String socketInfoOld = new String();
    /*This constrcutor is used for sending*/
    public NotifySocketInfo(String oldLabel, String socketInfo, String socketInfoOld,int type)
    {
        this.oldLabel = oldLabel;
        this.socketInfo = socketInfo;
        this.socketInfoOld = socketInfoOld;
        this.type = type;
    }

    /*This constrcutor is used for decoding*/
    public NotifySocketInfo(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.NOTIFY_SOCKETINFO);
        dout.writeInt(this.oldLabel.length());
        dout.write(this.oldLabel.getBytes());

        dout.writeInt(this.socketInfo.length());
        dout.write(this.socketInfo.getBytes());

        dout.writeInt(this.socketInfoOld.length());
        dout.write(this.socketInfoOld.getBytes());

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
        int oldLabelLength = din.readInt();
        byte[] oldLabelBytes = new byte[oldLabelLength];
        din.readFully(oldLabelBytes);
        this.oldLabel = new String(oldLabelBytes);

        int socketInfoLength = din.readInt();
        byte[] socketInfoLengthBytes = new byte[socketInfoLength];
        din.readFully(socketInfoLengthBytes);
        this.socketInfo = new String(socketInfoLengthBytes);

        int socketInfoOldLength = din.readInt();
        byte[] socketInfoOldBytes = new byte[socketInfoOldLength];
        din.readFully(socketInfoOldBytes);
        this.socketInfoOld = new String(socketInfoOldBytes);

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public String getOldLabel()
    {
        return this.oldLabel;
    }

    public String getSocketInfo()
    {
        return this.socketInfo;
    }

    public String getSocketInfoOld()
    {
        return this.socketInfoOld;
    }

}