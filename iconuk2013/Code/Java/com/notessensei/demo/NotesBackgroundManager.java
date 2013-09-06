/**
 * Modified and used with permission of Stephan H. Wissel
 * 
 * Original Code: http://www.wissel.net/blog/d6plinks/SHWL-99U64Q
 * 
 */

package com.notessensei.demo;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotesBackgroundManager implements Serializable {
    private static final long               serialVersionUID        = 1L;
    // # of threads running concurrently
    private static final int                THREADPOOLSIZE          = 10;
    private final ExecutorService           service                 = Executors.newFixedThreadPool(THREADPOOLSIZE);

    public NotesBackgroundManager() {
        // For use in managed beans
    }

    public void submitService(final Runnable taskDef) {
        if (taskDef == null) {
            System.err.println("submitService: NULL callable submitted to submitService");
            return;
        }
        // Execute runs without return
        this.service.execute(taskDef);
    }

    @Override
    protected void finalize() throws Throwable {
        if ((this.service != null) && !this.service.isTerminated()) {
            this.service.shutdown();
        }
        super.finalize();
    }
}

