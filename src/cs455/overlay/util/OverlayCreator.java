package cs455.overlay.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Random;

/*This class is used to form the overlay and assgin link weights to the overlay.

Overlay algorithm:
    if even, k = 2m , connect to m nearest nodes in each side for every node   
    if odd, k = 2m + 1 connect to opposite node and m nearest nodes in each size for every node
    
Link weights will be randomly assign between 1-10*/
public class OverlayCreator 
{
    private String[] nodeBuffer;
    private int numberConnection;
    private HashMap<String, Set<String>> mapper;
    private TreeMap<String, Integer> linkInfo;
    public OverlayCreator(Set<String> set, int numberConnection) throws Exception
    {
        Object[] obj = set.toArray();
        /* Triplicate the original node set to form 3 circle, 
        then we use the middle circle to do addiction and subtraction symmetrically,
         it can easily map the value we want*/
        this.nodeBuffer = new String[obj.length * 3];
        for(int i=0; i<nodeBuffer.length;i++) {
           this.nodeBuffer[i] = (String)obj[i % obj.length];
        }
        this.numberConnection = numberConnection;
        this.mapper = new HashMap<>();
        this.linkInfo = new TreeMap<>();
        createOverlay();
    }


    private boolean isValid()
    {
        //n >= k+1, nk is even
        if(nodeBuffer.length >= (numberConnection + 1) && ((nodeBuffer.length * numberConnection) %2) == 0)
        {
            return true;
        }
        return false;

    }

    private void createOverlay() throws Exception
    {
        if(!isValid())
        {
            System.err.println("The combination of n(order) and k(degree) is invalid! " + "\n" + 
            "Please type a valid one, n >= k+1, nk is even.");
            return;
        }

        for(int i=0; i<nodeBuffer.length/2;i++)
        {
            mapper.putIfAbsent(nodeBuffer[i], new HashSet<String>());
        }
        
        int m, count;
        int oneThird = nodeBuffer.length/3;
        for(int i = oneThird; i < oneThird + oneThird; i++)
        {
            m = numberConnection/2;
            count = 0;
            while(count < m)
            {
                count++;
                mapper.get(nodeBuffer[i]).add(nodeBuffer[(i + count)]);
                mapper.get(nodeBuffer[i]).add(nodeBuffer[(i - count)]);
            }
            /* if it is odd, connect the opposite*/
            if(numberConnection %2 != 0)
            {
                mapper.get(nodeBuffer[i]).add(nodeBuffer[(i + oneThird/2)]);
            }
        }

        assignLinkWeight();
    }

    /*Before assigning weights, remove the duplicate connections, for instance, 
    if B already in the connection list of A, then B's list won't have A in it. */
    private void assignLinkWeight()
    {
        HashSet<String> container = new HashSet<>();

        List<String> keyList = new ArrayList<String>(mapper.keySet());

        Set<String> value = new HashSet<>();

        for(int i = 0; i < keyList.size(); i++)
        {
            String key = keyList.get(i);
            /*value is current value Set<String> of key */
            value.clear();
            value.addAll(mapper.get(key));
            Iterator<String> itr = value.iterator(); 
            while (itr.hasNext())
            {
                String temp = itr.next();
                if(container.contains(temp)) 
                    mapper.get(key).remove(temp); 
            } 
            container.add(key);
        }

        /*Print out map for checking*/
        // mapper.entrySet().forEach(entry->{
        //     System.out.println(entry.getKey() + " " + entry.getValue());  
        //  });


        for(int i = 0; i < keyList.size(); i++)
        {
            String key = keyList.get(i);
            value.clear();
            value.addAll(mapper.get(key));
            Iterator<String> itr = value.iterator(); 
            while (itr.hasNext())
            {
                String temp = itr.next();
                linkInfo.putIfAbsent(key + "<=>" + temp, randomWeight());
            } 
        }
    }

    private int randomWeight()
    {
        Random random = new Random();
        /* random number from 1 to 10*/
        return (random.nextInt(10) + 1);
    }

    public TreeMap<String, Integer> getLinkInfo()
    {
        return this.linkInfo;
    }

    public HashMap<String, Set<String>> getMessagingNodeList()
    {
        return this.mapper;
    }

    /*Test the correcctness of the algorithm*/
    public static void main(String []args){
        HashSet<String> set=new HashSet<String>();  
        for(char alphabet = 'A'; alphabet <= 'J'; alphabet++ )
        {
            set.add(String.valueOf(alphabet));
        }
        try{
            OverlayCreator test = new OverlayCreator(set, 4);
            test.createOverlay();
            test.assignLinkWeight();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

     }

}
