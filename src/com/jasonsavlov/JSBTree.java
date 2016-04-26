package com.jasonsavlov;

import com.sun.istack.internal.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * JSBtree.java
 *
 * This is a BTree class.
 * Written by Jason Savlov.
 * Written using the guidance of the BTree.java found at:
 * http://algs4.cs.princeton.edu/code/edu/princeton/cs/algs4/BTree.java
 */

public class JSBTree
{

    private static final int MAX_CHILDREN = 4;

    private Node root;
    private int height;
    private int node_count;

    private final Lock mLock = new ReentrantLock();


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

    public synchronized boolean isEmpty()
    {
        return size() == 0;
    }

    public int size()
    {
        try {
            mLock.lock();
            return node_count;
        } finally {
            mLock.unlock();
        }
    }

    public int height()
    {
        try {
            mLock.lock();
            return height;
        } finally {
            mLock.unlock();
        }
    }

    private WordNode search(Node x, String key, int ht)
    {
        Entry[] children = x.children;

        try {
            mLock.lock();
            // Do we have an external node?
            if (ht == 0) {
                for (int i = 0; i < x.child_count; i++) {
                    if (equal(key, children[i].key)) {
                        return (WordNode) children[i].value;
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
        } finally {
            mLock.unlock();
        }
    }

    public WordNode get(String k)
    {
        if (k == null)
            throw new NullPointerException("The key must not be null.");

        return search(root, k, height);
    }

    public void add(String key)
    {
        WordNode existing;
        try {
            mLock.lock();
            existing = get(key);
        } finally {
            mLock.unlock();
        }

        try {
            mLock.lock();
            if (existing != null) {
                // It already exists.. increment the frequency and move on
                existing.frequency++;
                return;
            }

            put(key, new WordNode(key));
        } finally {
            mLock.unlock();
        }
    }

    public void put(String key, WordNode value)
    {
        try {
            mLock.lock();
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
        } finally {
            mLock.unlock();
        }
    }

    private Node insert(Node node, String key, WordNode value, int ht)
    {
        try {
            mLock.lock();
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
        } finally {
            mLock.unlock();
        }
    }

    private Node split(Node n)
    {
        try {
            mLock.lock();
            Node t = new Node(MAX_CHILDREN / 2);
            n.child_count = MAX_CHILDREN / 2;
            for (int i = 0; i < MAX_CHILDREN / 2; i++) {
                t.children[i] = n.children[MAX_CHILDREN / 2 + i];
            }
            return t;
        } finally {
            mLock.unlock();
        }
    }

    // A method that returns a List of WordNode objects
    public List<WordNode> treeToList()
    {
        ForkJoinPool workingPool = new ForkJoinPool();
        try {
            mLock.lock();
            List<WordNode> listToReturn = new ArrayList<>();
            TreeToListTask mainTask = new TreeToListTask(listToReturn, root);
            return workingPool.invoke(mainTask);
        } finally {
            mLock.unlock();
        }
    }

    private final class TreeToListTask extends RecursiveTask<List<WordNode>>
    {
        private final List<WordNode> workingList;
        private final Node assignedNode;

        private TreeToListTask(@NotNull List<WordNode> list,
                               @NotNull Node node)
        {
            this.workingList = list;
            this.assignedNode = node;
        }

        @Override
        protected List<WordNode> compute()
        {
            Node currentNode = assignedNode;
            Entry[] children = currentNode.children;
            List<TreeToListTask> nextNodes = new ArrayList<>();

            for (int i = 0; i < currentNode.child_count; i++)
            {
                Entry e = children[i];
                if (e.next != null) {
                    TreeToListTask nextTask = new TreeToListTask(workingList, e.next);
                    nextNodes.add(nextTask);
                    nextTask.fork();
                } else {
                    workingList.add((WordNode) e.value);
                }
            }

            for (TreeToListTask task : nextNodes)
            {
                task.join();
            }


            return workingList;
        }
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
