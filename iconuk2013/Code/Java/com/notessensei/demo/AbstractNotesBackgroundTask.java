/**
 * Modified and used with permission of Stephan H. Wissel
 * 
 * Original Code: http://www.wissel.net/blog/d6plinks/SHWL-99U64Q
 * 
 */

package com.notessensei.demo;

import java.util.ArrayList;
import java.util.Collection;

import lotus.domino.NotesException;
import lotus.domino.Session;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.SessionCloner;

public abstract class AbstractNotesBackgroundTask implements Runnable {
    protected final Session                     notesSession;
    protected final Collection<String>          callBackMessages;
    private SessionCloner                   sessionCloner;
    private NSFComponentModule              module;
    
    public AbstractNotesBackgroundTask() {
		this(null, null);
	}

    public AbstractNotesBackgroundTask(final Session optionalSession, final Collection<String> messageHandler) {
        this.callBackMessages = (messageHandler==null)?new ArrayList<String>():messageHandler;
        // optionalSession MUST be NULL when this should run in a thread, contain a session when
        // the class is running in the same thread as it was constructed
        this.notesSession = optionalSession;
        this.setDominoContextCloner();
    }

	public void run() {
        try {
            Session session;
            if (this.notesSession == null) {
                NotesContext context = new NotesContext(this.module);
                NotesContext.initThread(context);
                session = this.sessionCloner.getSession();
            } else {
                // We run in an established session
                session = this.notesSession;
            }
            
            this.callBackMessages.add("Background task run starting: " + this.getClass().toString());
            this.runNotes(session);
            
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (this.notesSession == null) {
                NotesContext.termThread();
                try {
                    this.sessionCloner.recycle();
                } catch (NotesException e1) {
                    e1.printStackTrace();
                }
            }
        }
        this.callBackMessages.add("Background task run completed: " + this.getClass().toString());
    }

    private void setDominoContextCloner() {
        // Domino stuff to be able to get a cloned session
        if (this.notesSession == null) {
            try {
                this.module = NotesContext.getCurrent().getModule();
                this.sessionCloner = SessionCloner.getSessionCloner();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    protected abstract void runNotes(Session session);
}