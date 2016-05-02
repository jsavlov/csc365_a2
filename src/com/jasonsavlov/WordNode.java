package com.jasonsavlov;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Created by jason on 4/21/16.
 */
public final class WordNode
{
    int frequency;
    final String value;

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public WordNode(String value)
    {
        this.frequency = 1;
        this.value = value;
    }

    public WordNode(String value, int initalFrequency)
    {
        this.value = value;
        this.frequency = initalFrequency;
    }

    public ByteBuffer getSerializedNode()
    {
        // The sequence of initial bytes
        byte[] initialBytes = {10, 10, 10};

        // The sequence of terminating bytes
        byte[] terminatingBytes = {1, 1, 1};

        // Convert the word value to a byte array
        byte[] valueBytes = value.getBytes(DEFAULT_CHARSET);
        int valueByteLength = valueBytes.length;

        // calculate the total size of the buffer
        int totalSizeOfBuffer = initialBytes.length
                + terminatingBytes.length
                + valueByteLength + 8;

        // Create the buffer
        ByteBuffer bufferToReturn = ByteBuffer.allocate(totalSizeOfBuffer);

        // Put the initial bytes
        bufferToReturn.put(initialBytes);

        // Put the int value of the valueBytes array length
        bufferToReturn.putInt(valueByteLength);

        // Put the value bytes
        bufferToReturn.put(valueBytes);

        // Put the frequency as an integer
        bufferToReturn.putInt(frequency);

        // Put the terminating bytes
        bufferToReturn.put(terminatingBytes);

        // Return the buffer
        return bufferToReturn;
    }

    public static WordNode nodeFromBytes(ByteBuffer buffer)
    {

        buffer.rewind();

        for (int i = 0; i < 3; i++)
        {
            byte b = buffer.get();
            if (b != 10) throw new InvalidByteSequenceException("The byte sequence of the node is invalid.");
        }

        int lengthOfValue = buffer.getInt();

        byte[] rawValueBytes = new byte[lengthOfValue];
        buffer.get(rawValueBytes);

        int frequency = buffer.getInt();

        for (int i = 0; i < 3; i++)
        {
            byte b = buffer.get();
            if (b != 1) throw new InvalidByteSequenceException("The byte sequence of the node is invalid.");
        }

        String valueStr = new String(rawValueBytes, DEFAULT_CHARSET);

        return new WordNode(valueStr, frequency);
    }

    static final class InvalidByteSequenceException extends RuntimeException
    {

        InvalidByteSequenceException(String msg)
        {
            super(msg);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        WordNode wn = (WordNode) obj;
        boolean valEqual = wn.value.equals(this.value);
        boolean freqEqual = wn.frequency == this.frequency;

        return valEqual && freqEqual;
    }
}
