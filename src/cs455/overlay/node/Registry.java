package cs455.overlay.node;

import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.util.*;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/*Registry class is responsible for:
1. Registring/Deregistring connections from messaging nodes.
2. Generate overlay and assign link weights then pass the information to all nodes
3. Each time the messaging node finish one rounding task, the final node will notify 
the registry about that, then the resgitry will notify the original node that this individual
task has finished, so the node can add the counter, a messaing node needs to make sure two parts
are finished: 1. sending all messages 2.all messages have successfully received.
4. Count the Task_Complete protocol for messaging nodes, if all messaging nodes have finished 
their work, issue a pull_traffic_summary protocol to all messaging nodes
5. Summarize all the statistics from messaging nodes and print them out.*/
public class Registry implements Node, Protocol {
    private int port;
    private OverlayCreator overlay;
    private ConnectionManager connectionManager;
    private AtomicInteger TaskCompleteCount;
    private AtomicInteger SummaryCompleteCount;
    private LinkedList<Tracker> nodeSummary; 
    public Registry(int port) {
        this.port = port;
        this.TaskCompleteCount = new AtomicInteger();
        this.SummaryCompleteCount = new AtomicInteger();
        this.connectionManager = new ConnectionManager();
        this.nodeSummary = new LinkedList<Tracker>();
        try {
        /*The server socket receiving capacity defaults to 20*/
            (new Thread(new TCPServerThread(20, port, this))).start();
            (new Thread(new TCPSenderThread(connectionManager.getQueue()))).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEvent(Event event, Socket socket) throws IOException, InterruptedException
    {
        switch(event.getType())
        {
            case Protocol.REGISTER_REQUEST:
                registrationHandler(event, socket, Protocol.REGISTER_REQUEST);
                break;
            case Protocol.DEREGISTER_REQUEST:
                registrationHandler(event, socket, Protocol.DEREGISTER_REQUEST);
                break;
            case Protocol.NODE_TASK_COMPLETE:
                onNodeTaskComplete(event);
                break;
            case Protocol.TASK_COMPLETE:
                TaskCompleteCount.incrementAndGet();
                if(this.TaskCompleteCount.intValue() == this.connectionManager.getHashtable().size())
                {
                    issueTrafficSummary();
                    TaskCompleteCount.set(0);
                }
                break;
            case Protocol.TRAFFIC_SUMMARY:
                this.SummaryCompleteCount.incrementAndGet();
                receiveTrafficSummary(event);
                synchronized(this)
                {
                    if(this.SummaryCompleteCount.intValue() == connectionManager.getHashtable().size())
                    {
                        pullTrafficSummary();
                        this.SummaryCompleteCount.set(0);
                        this.nodeSummary.clear();
                    }
                }
                break;
            default:
                System.err.println("Error!");
                break; 
        }
    }

    /* True for registration response, false for deregistration response*/
    private void registrationHandler(Event event, Socket socket, int type) throws IOException, InterruptedException
    {
        String key = ((Register)event).getIpAddress();
        if(type==Protocol.REGISTER_REQUEST)
        {
            if(!connectionManager.getHashtable().containsKey(key))
            {
                connectionManager.addUnit(key, socket);
                String info = "Registration request successful.";
                this.connectionManager.addTask(new Task(socket,(new RegisterResponse(info, (byte)100, Protocol.REGISTER_RESPONSE)).getBytes()));
            }
            else
            {
                String info = "Registration request unsuccessful.";
                this.connectionManager.addTask(new Task(socket,(new RegisterResponse(info, (byte)110, Protocol.REGISTER_RESPONSE)).getBytes()));
            }
        }
        else
        {
            if(connectionManager.getHashtable().remove(key, socket))
            {
                String info = "Deregistration request successful.";
                this.connectionManager.addTask(new Task(socket,(new RegisterResponse(info, (byte)100, Protocol.DEREGISTER_RESPONSE)).getBytes()));
            }
            else
            {
                String info = "Deregistration request unsuccessful.";
                this.connectionManager.addTask(new Task(socket,(new RegisterResponse(info, (byte)110, Protocol.DEREGISTER_RESPONSE)).getBytes()));
            }  
        }
    }

    private void onNodeTaskComplete(Event event) throws IOException, InterruptedException 
    {
        String original = ((NodeTaskComplete)event).getOriginal();
        this.connectionManager.addTask(new Task(connectionManager.getSocket(original),(new NodeTaskComplete(original, Protocol.NODE_TASK_COMPLETE)).getBytes()));
    }

    private void pullTrafficSummary()
    {  
        int nms = 0, nmr = 0; long ssm = 0, srm = 0;
        String format = "%1$-8s|%2$-25s|%3$-30s|%4$-28s|%5$-32s|%6$-28s\n";
        System.out.format(format, "", "Number of messages sent", "Number of messages received",
        "Summation of sent messages", "Summation of received messages", "Number of messages relayed");
        int count = 0;
        for(Tracker tracker: this.nodeSummary)
        {
            count++;
            System.out.format(format, "Node " + Integer.toString(count), String.valueOf(tracker.getSendTracker()), 
            String.valueOf(tracker.getReceiveTracker()), String.valueOf(tracker.getSendSummation()), 
            String.valueOf(tracker.getReceiveSummation()), String.valueOf(tracker.getRelayTracker()));

            nms += tracker.getSendTracker();
            nmr += tracker.getReceiveTracker();
            ssm += tracker.getSendSummation();
            srm += tracker.getReceiveSummation();
        }
        System.out.format(format, "Sum", String.valueOf(nms), String.valueOf(nmr), 
        String.valueOf(ssm), String.valueOf(srm), "");
    }

    private void receiveTrafficSummary(Event event) 
    {
        synchronized(this.nodeSummary) 
        {
            this.nodeSummary.add(((TrafficSummary)event).getTracker());
        }
    }
    
    private void setupOverlay(Scanner sc) throws Exception
    {
        int numberConnection = sc.nextInt();
        overlay = new OverlayCreator(connectionManager.getHashtable().keySet(), numberConnection);
        String wholeOverlay = new String();
        /*inform all the nodes about the whole overlay first*/
        for (String name : this.connectionManager.getHashtable().keySet())  
        {
            wholeOverlay = wholeOverlay + name + "<>";
        }
        wholeOverlay = wholeOverlay.substring(0,wholeOverlay.length()-2);

        HashMap<String, Set<String>> nodesNeededToConnect = new HashMap<>();
        nodesNeededToConnect = overlay.getMessagingNodeList();

        for (Map.Entry<String, Set<String>> mapElement : nodesNeededToConnect.entrySet()) { 
            String key = mapElement.getKey(); 
            InformNodesOverlay informNodesOverlay = new InformNodesOverlay(wholeOverlay, Protocol.INFORM__NODES_OVERLAY);
            this.connectionManager.addTask(new Task(connectionManager.getSocket(key), informNodesOverlay.getBytes()));
            MessagingNodesList messagingNodesList = new MessagingNodesList(mapElement.getValue(), key, Protocol.MESSAGING_NODES_LIST);
            this.connectionManager.addTask(new Task(connectionManager.getSocket(key), messagingNodesList.getBytes()));
        } 

    }

    private void issueTrafficSummary() throws IOException, InterruptedException 
    {
        PullTrafficSummary pullTrafficSummary = new PullTrafficSummary(Protocol.PULL_TRAFFIC_SUMMARY);
        for (Map.Entry<String,Socket> mapElement : connectionManager.getHashtable().entrySet()) { 
            String key = mapElement.getKey(); 
            this.connectionManager.addTask(new Task(connectionManager.getSocket(key), pullTrafficSummary.getBytes()));
        }
    }

    private void sendLinkWeights() throws InterruptedException, IOException {
        Link_Weights lw = new Link_Weights(overlay.getLinkInfo(), Protocol.LINK_WEIGHTS);
        for (Map.Entry<String,Socket> mapElement : connectionManager.getHashtable().entrySet()) { 
            String key = mapElement.getKey(); 
            this.connectionManager.addTask(new Task(connectionManager.getSocket(key), lw.getBytes()));
        } 

    }

    private void listWeights()
    {
        overlay.getLinkInfo().entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());  
         });
    }

    private void listMessagingNodes()
    {
        connectionManager.getHashtable().entrySet().forEach(entry->{
            System.out.println(entry.getKey());  
         });
    }

    private void startRounds(Scanner sc) throws InterruptedException, IOException {
        int numberRounds = sc.nextInt();
        TaskInitiate taskInitiate = new TaskInitiate(numberRounds, Protocol.TASK_INITIATE);
        for (Map.Entry<String,Socket> mapElement : connectionManager.getHashtable().entrySet()) { 
            String key = mapElement.getKey(); 
            this.connectionManager.addTask(new Task(connectionManager.getSocket(key), taskInitiate.getBytes()));
        }
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 1) {
            Registry registry = new Registry(Integer.parseInt(args[0]));
            Scanner sc = new Scanner(System.in);
            String command;
            while (!(command = sc.next()).equals("exit")) {
                switch (command) {
                case "list-messaging-nodes":
                    registry.listMessagingNodes();
                    break;
                case "list-weights":
                    registry.listWeights();
                    break;
                // case "send-overlay-link-weights":
                case "solw":
                    registry.sendLinkWeights();
                    break;
                case "setup-overlay":
                    registry.setupOverlay(sc);
                    break;
                case "start":
                    registry.startRounds(sc);
                    break;                
                default:
                    System.err.println("Unknown command");
                    break; 
                }
            }
            sc.close();
        }
        else
        {
            System.err.println("Incorrect number of parameters.");
            System.exit(1);
        }
    }
}