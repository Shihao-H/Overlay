package cs455.overlay.node;
import java.util.concurrent.atomic.LongAdder;

/*Tracker is used to do statistics*/
public class Tracker
{
    private LongAdder sendTracker;
    private LongAdder receiveTracker;
    private LongAdder relayTracker;
    private LongAdder sendSummation;
    private LongAdder receiveSummation;

    public Tracker()
    {
        this.sendTracker = new LongAdder();
        this.receiveTracker = new LongAdder();
        this.relayTracker = new LongAdder();
        this.sendSummation = new LongAdder();
        this.receiveSummation = new LongAdder();
    }

    public void addSendTracker()
    {
        this.sendTracker.add(1);
    }

    public void addReceiveTracker()
    {
        this.receiveTracker.increment();
    }

    public void addRelayTracker()
    {
        this.relayTracker.increment();
    }

    public void addSendSummation(int number)
    {
        this.sendSummation.add(number);
    }

    public void addReceiveSummation(int number)
    {
        this.receiveSummation.add(number);
    }

    public int getSendTracker()
    {
        return this.sendTracker.intValue();
    }

    public int getReceiveTracker()
    {
        return this.receiveTracker.intValue();
    }

    public int getRelayTracker()
    {
        return this.relayTracker.intValue();
    }

    public long getSendSummation()
    {
        return this.sendSummation.longValue();
    }

    public long getReceiveSummation()
    {
        return this.receiveSummation.longValue();
    }

    public void setSendTracker(int sendTracker)
    {
        this.sendTracker.reset();
        this.sendTracker.add(sendTracker);
    }

    public void setReceiveTracker(int receiveTracker)
    {
        this.receiveTracker.reset();
        this.receiveTracker.add(receiveTracker);
    }

    public void setRelayTracker(int relayTracker)
    {
        this.relayTracker.reset();
        this.relayTracker.add(relayTracker);
    }

    public void setSendSummation(long sendSummation)
    {
        this.sendSummation.reset();
        this.sendSummation.add(sendSummation);
    }

    public void setReceiveSummation(long receiveSummation)
    {
        this.receiveSummation.reset();
        this.receiveSummation.add(receiveSummation);
    }

    public void clear()
    {
        this.sendTracker.reset();
        this.receiveTracker.reset();
        this.relayTracker.reset();
        this.sendSummation.reset();
        this.receiveSummation.reset();
    }

}