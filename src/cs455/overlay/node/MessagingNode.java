package cs455.overlay.node;

import cs455.overlay.transport.*;
import cs455.overlay.util.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;
import java.lang.Thread;
import cs455.overlay.wireformats.*;
import java.net.InetAddress;
import java.util.Random;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class MessagingNode implements Node, Protocol {
    private final String host;
    private final int port;
    private final int localPort;
    private int rounds;
    private Socket registrySocket;
    private Tracker tracker;
    private InetAddress inetAddress;
    private ConnectionManager connectionManager;
    private TreeMap<String, Integer> linkInfo;
    private String[] nodesSet;
    private ShortestPath shortestPath;
    private HashMap<String, String> updateSocketMap;
    private boolean isFinishSending;
    private AtomicInteger remoteReceiveCount;
    private String localAddress;
    public MessagingNode(String host, int port) throws Exception {
        this.host = host;
        this.port = port;
        this.localPort = new Random().nextInt(3000)+7000;
        this.rounds = -1;
        this.tracker = new Tracker();
        this.inetAddress = InetAddress.getLocalHost();
        this.connectionManager = new ConnectionManager();
        this.linkInfo = new TreeMap<>();
        this.updateSocketMap = new HashMap<>();
        this.isFinishSending = false;
        this.remoteReceiveCount = new AtomicInteger();
        this.registrySocket = new Socket();
        this.localAddress = new String(inetAddress.getHostName()+": "+String.valueOf(localPort));
        try {
            (new Thread(new TCPServerThread(10, localPort, this))).start();
            (new Thread(new TCPSenderThread(connectionManager.getQueue()))).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        registerNode(host, port, Protocol.REGISTER_REQUEST);
    }

    private void registerNode(String host, int port, int type) throws IOException, InterruptedException {
        this.registrySocket = new Socket(host, port);
        System.out.println("This is node: " + this.inetAddress.getHostName());
        Register register = new Register(this.inetAddress.getHostName(), localPort, type);
        this.connectionManager.addTask(new Task(this.registrySocket, register.getBytes()));
        new Thread(new TCPReceiverThread(this.registrySocket, this)).start();
    }

    private void registerResponse(Event event, Socket socket) {
        System.out.println(((RegisterResponse) event).getInfo());
    }

    private void onMessagingNodesList(Event event) throws NumberFormatException, UnknownHostException, 
    InterruptedException, IOException{
        Socket socket;
        String str = ((MessagingNodesList)event).getInfo();

        String[] arrOfStr = str.split("<=>");
        String[] temp;
        /*get host and the nodes it needs to connect*/
        if(arrOfStr.length>1)
        {
            String[] nodes = arrOfStr[1].split(",");
            for(int i = 0; i < nodes.length;i++)
            {
                temp = nodes[i].split(": ");
                socket = new Socket(temp[0], Integer.parseInt(temp[1]));
                this.connectionManager.addUnit(nodes[i], socket);
                /*each time we connect to another socket, we will asssign a new port associate with it, 
                so we need a receiver thread listen to it*/
                new Thread(new TCPReceiverThread(socket, this)).start();

                NotifySocketInfo notifySocketInfo = new NotifySocketInfo(nodes[i],
                this.inetAddress.getHostName() + ": " + String.valueOf(socket.getLocalPort()), 
                this.localAddress, Protocol.NOTIFY_SOCKETINFO);
                this.connectionManager.addTask(new Task(socket, notifySocketInfo.getBytes()));
            }
        }
        else
            return;

    }

    private void onInformNodesOverlay(Event event)
    {
        this.nodesSet = ((InformNodesOverlay)event).getInfo().split("<>");
        for(String str: nodesSet)
        {
            System.out.println(str);
            updateSocketMap.putIfAbsent(str, str);
        }

    }

    private void onLinkWeights(Event event)
    {
        String[] holder = new String[2];
        String[] temp = ((Link_Weights)event).getInfo().split("\n");
        for(String value: temp)
        {
            holder = value.split("##");
            this.linkInfo.put(holder[0], Integer.parseInt(holder[1]));
        }

        this.shortestPath = new ShortestPath(linkInfo,nodesSet);
        this.shortestPath.dijkstra(this.localAddress);

    }

    private void printShortesPath()
    {
        this.shortestPath.printOut();
    }

    private void onTaskInitiate(Event event) throws IOException, InterruptedException 
    {
         this.rounds = ((TaskInitiate)event).getRounds();
         this.isFinishSending = false;
         /*Each round will select one node, each node will send 5 messages*/
         this.remoteReceiveCount.set(this.rounds * 5);
         //True: select random node withint he overlay
         //False: select random integer as payload
         int index, payload; 
         String holderString = new String();
         int remain;
         for(int i = 0; i < rounds; i++)
         {
            for(int j = 0; j < 5 ; j++)
            {
                while(true)
                {
                    holderString="";
                    remain = 0;
                    index = random(true);
                    /*nodeSet[index] is the destination*/
                    if(!nodesSet[index].equals(this.localAddress))
                    {
                        payload = random(false);
                        this.tracker.addSendSummation(payload);
                        this.tracker.addSendTracker();
                        String head = shortestPath.getRountingCache().get(nodesSet[index]).getFirst();
                        for(String str: shortestPath.getRountingCache().get(nodesSet[index]))
                        {   
                            holderString = holderString + "<=>" + str;
                            remain++;
                        }
                        remain--;
                        // & head!=null
                        if(!holderString.isEmpty())
                        {
                            holderString = holderString.substring(3);
                            Message message = new Message(payload, holderString, this.localAddress,remain,Protocol.MESSAGE);
                            connectionManager.addTask(new Task(connectionManager.getSocket(updateSocketMap.get(head)), message.getBytes()));                        
                        }
                        break;
                    }
                }   
            }
        }
        this.isFinishSending = true;
        System.out.println("Finish all tasks sending"); 
    }

    private void onMessage(Event event) throws IOException, InterruptedException 
    {

        int payload = ((Message)event).getPayload();
        int remain = ((Message)event).getRemain();
        String original = ((Message)event).getOriginal();
        String holderString = new String();


        if(remain>1)
        {
            this.tracker.addRelayTracker();
            String[] rountingCache = ((Message)event).getRountingCache().split("<=>");
            for(int i = 1; i< rountingCache.length; i++)
            {
                holderString = rountingCache[i] + "<=>" + holderString;
            }
            /*rountingCache[0] means is the current node, rountingCache[1] is the next node*/
            holderString = holderString.substring(0, holderString.length()-3);
            Message message = new Message(payload, holderString, original, --remain, Protocol.MESSAGE);
            connectionManager.addTask(new Task(connectionManager.getSocket(updateSocketMap.get(rountingCache[1])), message.getBytes()));
        }
        else
        {
            this.tracker.addReceiveTracker();
            this.tracker.addReceiveSummation(payload);
            NodeTaskComplete nodeTaskComplete = new NodeTaskComplete(original, Protocol.NODE_TASK_COMPLETE);
            this.connectionManager.addTask(new Task(this.registrySocket, nodeTaskComplete.getBytes()));
        }  
    }

    private void onTaskComplete() throws IOException, InterruptedException 
    {
        this.remoteReceiveCount.decrementAndGet();
        if(this.isFinishSending && this.remoteReceiveCount.intValue()==0)
        {
            TaskComplete taskComplete = new TaskComplete(Protocol.TASK_COMPLETE);
            this.connectionManager.addTask(new Task(this.registrySocket, taskComplete.getBytes()));
        }
    }

    private void onNotifySocketInfo(Event event, Socket socket)
    {
        String socketInfo = ((NotifySocketInfo)event).getSocketInfo();
        String oldLabel = ((NotifySocketInfo)event).getOldLabel();
        String socketInfoOld = ((NotifySocketInfo)event).getSocketInfoOld();
        this.connectionManager.addUnit(socketInfo, socket);
        this.updateSocketMap.replace(socketInfoOld,socketInfoOld,socketInfo);

    }

    private void onNodeTaskComplete() throws IOException, InterruptedException 
    {
       onTaskComplete();
    }

    private void sendTrafficSummary() throws IOException, InterruptedException 
    {
        TrafficSummary trafficSummary = new TrafficSummary(this.tracker, Protocol.TRAFFIC_SUMMARY);
        this.connectionManager.addTask(new Task(this.registrySocket, trafficSummary.getBytes()));
        synchronized(this.tracker)
        {
            this.tracker.clear();
        }
    }
    
    private int random(boolean bool)
    {
        Random r=new Random();
        if(bool)
        {
            return r.nextInt(nodesSet.length);
        }
        return r.nextInt();
    }

    // private void forceSend() throws IOException, InterruptedException 
    // {
    //     this.remoteReceiveCount = 3;
    //     for (Map.Entry mapElement : connectionManager.getHashtable().entrySet()) { 
    //         String key = (String)mapElement.getKey(); 
    //         System.out.println("send to: " + key); 
    //         Message message = new Message(2, key, this.localAddress, 1, Protocol.MESSAGE);
    //         connectionManager.addTask(new Task(connectionManager.getSocket(key), message.getBytes()));
    //     } 
    // }

    @Override 
    public void onEvent(Event event, Socket socket) throws IOException, InterruptedException
    {
        switch(event.getType())
        {
            case Protocol.REGISTER_RESPONSE:
                registerResponse(event, socket);
                break;
            case Protocol.DEREGISTER_RESPONSE:
                registerResponse(event, socket);
                break;
            case Protocol.MESSAGING_NODES_LIST:
                onMessagingNodesList(event);
                break;
            case Protocol.LINK_WEIGHTS:
                onLinkWeights(event);
                break;
            case Protocol.INFORM__NODES_OVERLAY:
                onInformNodesOverlay(event);
                break;
            case Protocol.NOTIFY_SOCKETINFO:
                onNotifySocketInfo(event,socket);
                break;
            case Protocol.TASK_INITIATE:
                onTaskInitiate(event);
                break;
            case Protocol.MESSAGE:
                onMessage(event);
                break;
            case Protocol.NODE_TASK_COMPLETE:
                onNodeTaskComplete();
                break;
            case Protocol.PULL_TRAFFIC_SUMMARY:
                sendTrafficSummary();
                break;
            default:
                System.err.println("Error!");
                break; 
        }
    }


    public static void main(String[] args)
    {
        String host;    
        int port;
        if (args.length == 2)
        {
            try
            {
                host = args[0];
                port = Integer.parseInt(args[1]);
                try
                {
                    MessagingNode mNode = new MessagingNode(host,port);
                    Scanner sc = new Scanner(System.in);
                    String command;
                    while(!(command = sc.next()).equals("exit"))
                    {
                        switch(command)
                        { 
                            case "exit-overlay":
                                mNode.registerNode(mNode.host, mNode.port, Protocol.DEREGISTER_REQUEST);
                                break;
                            case "print-shortest-path":
                                mNode.printShortesPath();
                                break;
                            default:
                                System.err.println("Unknown command");
                                break; 
                        }
                    }
                    sc.close();
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            } 
            catch (Exception e)
            {
                System.err.println("Argument 1 must be a String. Argument 2 must be an Integer.");
                System.exit(1);
            }
        }
        else
        {
            System.err.println("Incorrect number of parameters.");
            System.exit(1);
        }
    }


}