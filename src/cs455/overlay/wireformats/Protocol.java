package cs455.overlay.wireformats;


public interface Protocol
{
    final int REGISTER_REQUEST = 0;
    
    final int REGISTER_RESPONSE = 1;

    final int DEREGISTER_REQUEST = 2;
    
    final int DEREGISTER_RESPONSE = 3;

    final int MESSAGING_NODES_LIST = 4;
    
    final int INFORM__NODES_OVERLAY= 5;

    final int LINK_WEIGHTS = 6;

    final int TASK_INITIATE = 7;

    final int NOTIFY_SOCKETINFO = 8;

    final int MESSAGE = 9;

    /*if one message is finished, we send it to the registry and registry notify the original node to add the count, 
    the count is used for checking the message it should send*/
    final int NODE_TASK_COMPLETE = 10;

    final int TASK_COMPLETE = 11;

    final int PULL_TRAFFIC_SUMMARY = 12; 

    final int TRAFFIC_SUMMARY = 13; 

    final byte SUCCESS = (byte)100;

    final byte FAILURE = (byte)110;
}