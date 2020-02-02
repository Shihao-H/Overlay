package cs455.overlay.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class ShortestPath
{
    private int vs;
    private int [] prev;
    private int [] dist;
    private String[] mVexs;       // 顶点集合
    private int[][] mMatrix;    // 邻接矩阵
    private static final int INF = Integer.MAX_VALUE;   // 最大值
    private ArrayList<String> cachePrintOut;  
    private Hashtable<String, LinkedList<String>> rountingCache;
    private Hashtable<String, Integer> mapTable;
    public ShortestPath(TreeMap<String, Integer> treeMap, String[] nodes)
    {
        this.vs = -1;
        this.dist = new int[nodes.length];
        this.prev = new int[nodes.length];
        this.mVexs = new String[nodes.length];

        for (int i=0; i<nodes.length; i++) 
            mVexs[i] = nodes[i]; 

        this.mMatrix = new int[nodes.length][nodes.length];

        for (int row = 0; row < mMatrix.length; row++) {
            for (int col = 0; col < mMatrix[row].length; col++) {
                mMatrix[row][col] = INF;
            }
         }

        this.mapTable = new Hashtable<String, Integer>();
        this.rountingCache = new Hashtable<String, LinkedList<String>>();
        this.cachePrintOut = new ArrayList<String>();
        fillTable(nodes);
        convertToArr(treeMap);
    }

    private void fillTable(String[] nodes)
    {
        int count = 0;
        for (String key : nodes) {
            mVexs[count] = key;
            mapTable.putIfAbsent(key, count++);
         }
    }

    //convert the elements in TreeMap to data structure array.
    private void convertToArr(TreeMap<String, Integer> treeMap)
    {

        for(Map.Entry<String,Integer> entry : treeMap.entrySet()) {
            String key = entry.getKey();
            String [] nodes = key.split("<=>");
            Integer value = entry.getValue();

            mMatrix[mapTable.get(nodes[0])][mapTable.get(nodes[1])] = value;
            mMatrix[mapTable.get(nodes[1])][mapTable.get(nodes[0])] = value;
          }
    }

    public void dijkstra(String head) {
        this.vs = mapTable.get(head);
        // flag[i]=true表示"顶点vs"到"顶点i"的最短路径已成功获取
        boolean[] flag = new boolean[mVexs.length];
    
        // 初始化
        for (int i = 0; i < mVexs.length; i++) {
            flag[i] = false;          // 顶点i的最短路径还没获取到。
            dist[i] = mMatrix[vs][i];  // 顶点i的最短路径为"顶点vs"到"顶点i"的权。
            if(dist[i] == INF) 
                prev[i] = -1; //not accessible
            else 
                prev[i] = vs;
        }
    
        // 对"顶点vs"自身进行初始化
        flag[vs] = true;
        dist[vs] = 0;
        prev[vs] = vs;
    
        // 遍历mVexs.length-1次；每次找出一个顶点的最短路径。
        int k=0;
        for (int i = 1; i < mVexs.length; i++) {
            // 寻找当前最小的路径；
            // 即，在未获取最短路径的顶点中，找到离vs最近的顶点(k)。
            int min = INF;
            for (int j = 0; j < mVexs.length; j++) {
                if (flag[j]==false && dist[j]<min) {
                    min = dist[j];
                    k = j;
                }
            }
            // 标记"顶点k"为已经获取到最短路径
            flag[k] = true;
    
            // 修正当前最短路径和前驱顶点
            // 即，当已经"顶点k的最短路径"之后，更新"未获取最短路径的顶点的最短路径和前驱顶点"。
            for (int j = 0; j < mVexs.length; j++) {
                int tmp = (mMatrix[k][j]==INF ? INF : (min + mMatrix[k][j]));
                if (flag[j]==false && (tmp<dist[j]) ) {
                    dist[j] = tmp;
                    prev[j] = k;
                }
            }
        }
    

        int current;
        int index;
        String hold = new String();
        for (int i=0; i < mVexs.length; i++)
        {
            if(i==vs) continue;
            this.rountingCache.putIfAbsent(mVexs[i], new LinkedList<String>());
            current = i;
            index = prev[i];
            hold = "";
            hold = mVexs[i];
            while(true)
            {
                hold = hold + "--" + String.valueOf(mMatrix[current][index]) + "--" + mVexs[index];
                if(index == vs)
                {
                    break;
                }
                current = index;
                index = prev[index];
            }
            String[] toRountingCache = hold.split("--([1-9]|10)--");

            /*exclude the node itself from the rounting cache*/
            for (int counter = toRountingCache.length - 2; counter >= 0; counter--)
            {
                this.rountingCache.get(mVexs[i]).add(toRountingCache[counter]);
            }
            hold = hold + " Total weight: " + dist[i];
            cachePrintOut.add(hold);

        }

        System.out.println(rountingCache.toString());
    }

    public void printOut()
    {
        // 打印dijkstra最短路径的结果
        System.out.println("Dijkstra result for " + mVexs[vs]);
        for (String printOut: this.cachePrintOut) { 
            System.out.println(printOut); 
        } 

    }

    public Hashtable<String, LinkedList<String>> getRountingCache()
    {
        return this.rountingCache;
    }

}