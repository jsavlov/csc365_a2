package com.jasonsavlov;

import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class WordNodeTest
{
    private final Charset char_set = StandardCharsets.UTF_8;
    private final String testWordStr = "Test";
    private byte[] testWordBytes = testWordStr.getBytes(char_set);
    private final int testFrequency = 41;

    private WordNode mainTestNode = null;   // The main node that will be serialized
    private ByteBuffer mainTestByteBuffer = null;



    @Before
    public void setUp() throws Exception
    {
        System.out.println("Setting up WordNodeTest...");

        // Create the WordNode to test against serialization
        mainTestNode = new WordNode(testWordStr, testFrequency);

        mainTestByteBuffer = ByteBuffer.allocate(testWordBytes.length + 14);

        byte[] initialBytes = {10, 10, 10};
        byte[] terminatingBytes = {1, 1, 1};

        mainTestByteBuffer.put(initialBytes);
        mainTestByteBuffer.putInt(testWordBytes.length);
        mainTestByteBuffer.put(testWordBytes);
        mainTestByteBuffer.putInt(testFrequency);
        mainTestByteBuffer.put(terminatingBytes);
    }

    @Test
    public void testGetSerializedNode() throws Exception
    {
        String value = "Test";
        int frequency = 41;

        WordNode workingTestNode = new WordNode(value);
        workingTestNode.frequency = frequency;

        ByteBuffer workingTestByteBuffer = workingTestNode.getSerializedNode();

        byte[] workingTestByteArray = workingTestByteBuffer.array();
        assertArrayEquals(mainTestByteBuffer.array(), workingTestByteArray);
    }

    @Test
    public void testNodeFromBytes() throws Exception
    {
        String value = "Test";
        int frequency = 41;

        byte[] valueBytes = value.getBytes();
        ByteBuffer workingTestBuffer = ByteBuffer.allocate(valueBytes.length + 14);

        byte[] initialBytes = {10, 10, 10};
        byte[] terminatingBytes = {1, 1, 1};

        workingTestBuffer.put(initialBytes);
        workingTestBuffer.putInt(valueBytes.length);
        workingTestBuffer.put(valueBytes);
        workingTestBuffer.putInt(frequency);

        workingTestBuffer.put(terminatingBytes);

        WordNode workingNodeFromBytes = WordNode.nodeFromBytes(workingTestBuffer);

        assertTrue(workingNodeFromBytes.equals(mainTestNode));
    }
}