package com.jasonsavlov.btree;

import java.util.Comparator;

/**
 * JSBtree.java
 *
 * This is a BTree class.
 * Written by Jason Savlov.
 * Written using the guidance of the BTree.java found at:
 * http://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/BTree.java
 */

public class JSBTree<Key extends Comparable<Key>, Value>
{

    private static final int MAX_CHILDREN = 4;

    private Node root;
    private int height;
    private int node_count;



    private static final class Node
    {
        private int child_count; // count of children
        private Entry[] children = new Entry[MAX_CHILDREN];

        private Node(int k)
        {
            child_count = k;
        }
    }

    private static class Entry
    {
        private Comparable key;
        private Object value;
        private Node next;

        public Entry(Comparable key, Object value, Node next)
        {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public JSBTree()
    {
        root = new Node(0);
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public int size()
    {
        return node_count;
    }

    public int height()
    {
        return height;
    }

    private Value search(Node x, Key key, int ht)
    {
        Entry[] children = x.children;

        // Do we have an external node?
        if (ht == 0) {
            for (int i = 0; i < x.child_count; i++) {
                if (equal(key, children[i].key)) {
                    return (Value) children[i].value;
                }
            }
        }

        // If not, we have an internal node!
        else {
            for (int i = 0; i < x.child_count; i++) {
                if ((i+1) == x.child_count || lessThan(key, children[i+1].key)) {
                    return search(children[i].next, key, ht - 1);
                }
            }
        }

        // all else, return nothin'.
        return null;
    }

    public Value get(Key k)
    {
        if (k == null)
            throw new NullPointerException("The key must not be null.");

        return search(root, k, height);
    }

    public void put(Key key, Value value)
    {
        if (key == null) {
            throw new NullPointerException("The key must not be null.");
        }

        Node u = insert(root, key, value, height);
        node_count++;
        if (u == null)
            return;

        // split it if needed
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, null, root);
        t.children[1] = new Entry(u.children[0].key, null, u);
        root = t;
        height++;
    }

    private Node insert(Node node, Key key, Value value, int ht)
    {
        int i;
        Entry e = new Entry(key, value, null);

        // external
        if (ht == 0) {
            for (i = 0; i < node.child_count; i++) {
                if (lessThan(key, node.children[i].key))
                    break;
            }
        }

        // internal
        else {
            for (i = 0; i < node.child_count; i++) {
                if (((i + 1) == node.child_count) || lessThan(key, node.children[i].key)) {
                    Node r = insert(node.children[i++].next, key, value, ht - 1);
                    if (r == null) {
                        return null;
                    }
                    e.key = r.children[0].key;
                    e.next = r;
                    break;
                }
            }
        }

        for (int j = node.child_count; j > i; j--) {
            node.children[j] = node.children[j - 1];
        }
        node.children[i] = e;
        node.child_count++;
        if (node.child_count < MAX_CHILDREN)
            return null;
        else
            return split(node);
    }

    private Node split(Node n)
    {
        Node t = new Node(MAX_CHILDREN / 2);
        n.child_count = MAX_CHILDREN / 2;
        for (int i = 0; i < MAX_CHILDREN / 2; i++) {
            t.children[i] = n.children[MAX_CHILDREN / 2 + i];
        }
        return t;
    }


    /*
        Helper comparison methods
     */

    private boolean equal(Comparable key1, Comparable key2)
    {
        return key1.compareTo(key2) == 0;
    }

    private boolean lessThan(Comparable key1, Comparable key2)
    {
        return key1.compareTo(key2) < 0;
    }





}
