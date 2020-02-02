package cs455.overlay.node;

import java.io.IOException;
import java.net.Socket;

import cs455.overlay.wireformats.Event;

public interface Node 
{ 
	void onEvent(Event event, Socket socket) throws IOException, InterruptedException;
} 