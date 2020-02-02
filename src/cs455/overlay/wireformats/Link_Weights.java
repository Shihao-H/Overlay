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

/*Send the link-weights infomation to messaging nodes so they can cache and use that in future rounding*/
public class Link_Weights implements Event, Protocol
{
    private TreeMap<String, Integer> linkInfo = new TreeMap<>();
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String fullString = "";
    /*This constrcutor is used  for sending*/
    public Link_Weights(TreeMap<String, Integer> linkInfo, int type)
    {
        this.type = type;
        this.linkInfo = linkInfo;
    }

    /*This constrcutor is used  for decoding*/
    public Link_Weights(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
        String concat = new String();

        for(Map.Entry<String,Integer> entry : linkInfo.entrySet()) {
            concat = concat + entry.getKey() + "##" + String.valueOf(entry.getValue()) + "\n"; 
        }

        concat = concat.substring(0, concat.length()-1);

        byte[] link_info = concat.getBytes();
        dout.writeInt(Protocol.LINK_WEIGHTS);
        dout.writeInt(link_info.length);
        dout.write(link_info);


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
        fullString = new String(identifierBytes);

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public String getInfo()
    {
        return this.fullString;
    }

}