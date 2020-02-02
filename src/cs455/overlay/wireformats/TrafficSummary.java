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

/*Individual nodes send their traffic summaries to registry.*/
public class TrafficSummary implements Event, Protocol
{
    private byte[] marshalledBytes = new byte[0];
    private int type = -1;
    private Tracker tracker = new Tracker();
    /*This constrcutor is used for sending*/
    public TrafficSummary(Tracker tracker, int type)
    {
        this.tracker = tracker;
        this.type = type;
    }

    /*This constrcutor is used for decoding*/
    public TrafficSummary(byte[] marshalledBytes) throws IOException
    {
        this.marshalledBytes = marshalledBytes;
        this.unMarshall();
    }

    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(Protocol.TRAFFIC_SUMMARY);

        dout.writeInt(tracker.getSendTracker());
        dout.writeInt(tracker.getReceiveTracker());
        dout.writeInt(tracker.getRelayTracker());

        dout.writeLong(tracker.getSendSummation());
        dout.writeLong(tracker.getReceiveSummation());

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
        synchronized(this.tracker)
        {
            this.tracker.setSendTracker(din.readInt());
            this.tracker.setReceiveTracker(din.readInt());
            this.tracker.setRelayTracker(din.readInt());
            this.tracker.setSendSummation(din.readLong());
            this.tracker.setReceiveSummation(din.readLong());
        }

        baInputStream.close();
        din.close();
    }
    
    @Override
    public int getType() { 
        return this.type;
    } 

    public Tracker getTracker()
    {
        return this.tracker;
    }

}