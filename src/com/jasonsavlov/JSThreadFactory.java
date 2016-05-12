package com.jasonsavlov;

import java.util.concurrent.ThreadFactory;

/**
 * Created by jason on 5/12/16.
 */
public class JSThreadFactory implements ThreadFactory
{
    private final String prefix;
    private int thread_count = 0;
    public JSThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        return new Thread(r, prefix + "_" + (++thread_count));
    }
}

