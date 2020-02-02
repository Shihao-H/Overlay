package cs455.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/*After remove the redundant duplicate nodes in connection map, 
notify all messaging nodes so they can use this to form connections with each other
the format may be something like below:

jupiter: 7979 [salem: 8133, london: 9965, dover: 8495]
jackson: 9816 [seoul: 9873, berlin: 8038, beijing: 9880]
santa-fe: 8045 [singapore: 8441, seoul: 9873, berlin: 8038]
singapore: 8441 [london: 9965, salem: 8133]
london: 9965 [beijing: 9880]
salem: 8133 [berlin: 8038]
seoul: 9873 [dover: 8495]
dover: 8495 [beijing: 9880]
beijing: 9880 []
berlin: 8038 []

base on the list some nodes need to connect the other nodes, so don't have nodes to connect, 
so they will accept connections, and we will use another Event InformNodesOverlay to connect back.
*/
public class MessagingNodesList implements Event, Protocol
{
    private Set<String> nodeSet = new HashSet<String>();
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private String key = "";
    private String fullString = "";
    /*This constrcutor is used  for sending*/
    public MessagingNodesList(Set<String> nodeSet, String key, int type)
    {
        this.nodeSet = nodeSet;
        this.key = key;
        this.type = type;
    }

    /*This constrcutor is used  for decoding*/
    public MessagingNodesList(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        byte[] messagingnode_info = new String(key+"<=>"+String.join(",", nodeSet)).getBytes();
        dout.writeInt(Protocol.MESSAGING_NODES_LIST);
        dout.writeInt(messagingnode_info.length);
        dout.write(messagingnode_info);
        
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
        this.fullString = new String(identifierBytes);
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