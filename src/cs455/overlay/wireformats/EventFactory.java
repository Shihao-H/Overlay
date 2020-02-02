package cs455.overlay.wireformats;

import java.io.IOException;
import java.nio.ByteBuffer;

/* Singleton class, this class is used for generating Events. */
public class EventFactory {
    private static EventFactory instance = new EventFactory();

    private EventFactory() {
    }

    public static EventFactory getInstance() {
        return instance;
    }

    public Event generateEvent(byte[] marshalledBytes) throws IOException
    {
        /* first integer indicates protocol */
        switch(ByteBuffer.wrap(marshalledBytes, 0, 4).getInt())
        {
            case Protocol.REGISTER_REQUEST:
                return new Register(marshalledBytes);
            case Protocol.REGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            case Protocol.DEREGISTER_REQUEST:
                return new Register(marshalledBytes);
            case Protocol.DEREGISTER_RESPONSE:
                return new RegisterResponse(marshalledBytes);
            case Protocol.MESSAGING_NODES_LIST:
                return new MessagingNodesList(marshalledBytes);
            case Protocol.LINK_WEIGHTS:
                return new Link_Weights(marshalledBytes);
            case Protocol.INFORM__NODES_OVERLAY:
                return new InformNodesOverlay(marshalledBytes);
            case Protocol.NOTIFY_SOCKETINFO:
                return new NotifySocketInfo(marshalledBytes);
            case Protocol.TASK_INITIATE:
                return new TaskInitiate(marshalledBytes);
            case Protocol.MESSAGE:
                return new Message(marshalledBytes);
            case Protocol.NODE_TASK_COMPLETE:
                return new NodeTaskComplete(marshalledBytes);
            case Protocol.TASK_COMPLETE:
                return new TaskComplete(marshalledBytes);
            case Protocol.PULL_TRAFFIC_SUMMARY:
                return new PullTrafficSummary(marshalledBytes);
            case Protocol.TRAFFIC_SUMMARY:
                return new TrafficSummary(marshalledBytes);
            default:
                System.err.println("Unknown type");
                break; 
        }
        return null;
    }
}