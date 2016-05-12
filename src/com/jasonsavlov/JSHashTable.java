package com.jasonsavlov;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Created by jason on 2/23/16.
 */
public class JSHashTable implements Serializable
{
    // The default bucket count, in case one isn't provided in the constructor
    public static final int DEFAULT_BUCKET_COUNT = 128;

    // Instance variables
    private volatile int entry_count = 0;
    private volatile int bucket_count = 0;

    public boolean has_modifications = false;

    // buckets for Nodes
    private Node[] buckets;

    // Internal Node class used as buckets for maintaining word frequency
    private static class Node implements Serializable
    {
        String key;
        WebPage value;
        Node next;

        public Node(String k, WebPage v) {
            key = k;
            value = v;
        }

        public Node addNode(String k, WebPage v) {
            if (this.next != null) {
                return this.next.addNode(k, v);
            } else {
                this.next = new Node(k, v);
                return this.next;
            }
        }

        public Node(Node n) {
            key = n.key;
            value = n.value;
        }
    }

    public JSHashTable()
    {
        bucket_count = DEFAULT_BUCKET_COUNT;
        buckets = new Node[bucket_count];
    }

    public JSHashTable(int initial_buckets)
    {
        bucket_count = initial_buckets;
        buckets = new Node[bucket_count];
    }


    public int getHash(String k)
    {
        return Math.abs(k.hashCode()) % bucket_count;
    }

    public synchronized boolean add(String k, WebPage v)
    {
        if ((float) entry_count / (float) bucket_count > 0.66666667) {
            this.resizeTable();
        }

        int hash = getHash(k);
        int bucket_pos = Math.abs(hash % bucket_count);

        Node node = buckets[bucket_pos];
        if (node != null) {
            while (node.next != null) {
                if (node.key.equalsIgnoreCase(k)) {
                    return false;
                }
                node = node.next;
            }
            node.next = new Node(k, v);
            this.entry_count++;
            return true;
        } else {
            buckets[bucket_pos] = new Node(k, v);
            this.entry_count++;
            return true;
        }
    }

    public synchronized boolean contains(String k) {
        int hash = getHash(k);
        Node n = buckets[hash];
        while (n != null) {
            if (n.key.equalsIgnoreCase(k)) {
                return true;
            } else {
                n = n.next;
            }
        }
        return false;
    }

    private synchronized int resizeTable()
    {
        int newSize = bucket_count * 2;
        Node[] newBuckets = new Node[newSize];

        for (Node n : this.buckets)
        {
            if (n == null) continue;

            String nKey = n.key;
            Node cNode = n;
            do {
                int newHash = getHash(nKey);
                newBuckets[newHash % newSize] = new Node(n);

                cNode = cNode.next;
            } while (cNode != null);
        }

        this.buckets = newBuckets;
        this.bucket_count = newSize;

        return newSize;
    }

    public synchronized List<WebPage> getTableAsList()
    {
        List<WebPage> listToReturn = new ArrayList<>();

        for (Node workingNode : buckets)
        {
            Node currentNode = workingNode;

            if (workingNode == null) {
                continue;
            }

            do {
                WebPage wp = currentNode.value;
                listToReturn.add(wp);
            } while ((currentNode = currentNode.next) != null);
        }

        return listToReturn;

    }

}
