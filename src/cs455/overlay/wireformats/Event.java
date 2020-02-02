package cs455.overlay.wireformats;

import java.io.IOException;

/*All events has two constructors, one is used for constructing the fields we want, then we do a getBytes() to 
require the bytes we need and form a Task object and add it to the queue, another constrcutor is used when the 
receiver thread receives the bytes and use them to form an Event object, inside the constructor it will call the
 unMarshall() to decode the bytes. Then we do a node.onEvent() to handle the Event on nodes.*/
public interface Event
{
    byte[] getBytes() throws IOException; 
    void unMarshall() throws IOException;
	int getType(); 
    
}