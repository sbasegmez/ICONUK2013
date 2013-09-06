/**
 * Sorry about terrible coding...
 */

package com.developi.iconuk13.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import lotus.domino.Session;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.notessensei.demo.AbstractNotesBackgroundTask;
import com.notessensei.demo.NotesBackgroundManager;

public class AnalyzerQueue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, Analyzer> myQueue=new TreeMap<String, Analyzer>();
		
	public AnalyzerQueue() {
	}

	public void addAnalyzer(Analyzer a) {
		myQueue.put(a.getName(), a);
	}
	
	public void addAndRunAnalyzer(Analyzer a) {
		addAnalyzer(a);
		a.findUACounts(ExtLibUtil.getCurrentSession());
	}

	public void addAndForgetAnalyzer(final Analyzer a) {
		addAnalyzer(a);
		
		NotesBackgroundManager appManager=(NotesBackgroundManager) ExtLibUtil.resolveVariable(FacesContext.getCurrentInstance(), "appManager");
		
		appManager.submitService(new AbstractNotesBackgroundTask() { 
			@Override
			protected void runNotes(Session session) {
				a.findUACounts(session);				
			}
		});
		
		
	}
	
	public List<String> getNames() {
		return new ArrayList<String>(myQueue.keySet());
	}
	
	public Analyzer byName(String name) {
		return myQueue.get(name);
	}
	
	public String getQueueJSON() {
		StringBuffer result=new StringBuffer();
		boolean first=true;
		
		result.append("[");
		
		for (Map.Entry<String, Analyzer> entry : myQueue.entrySet()) {
			if(first) {
				first=false;
			} else {
				result.append(",");
			}
			
			Analyzer a=entry.getValue();
			
		    result.append("{");
		    result.append("'name':'"+a.getName()+"',");
		    result.append("'detail':'"+a.getDetail()+"',");
		    result.append("'status':'"+a.getStatusText()+"',");
		    result.append("'completed':"+a.isDone()+",");
		    result.append("'elapsedTime':'"+a.getElapsedTime()+" seconds',");
		    result.append("'progress':"+a.getProgress());
		    result.append("}");
		}
		
		result.append("]");

		return result.toString();

	}
	
	public boolean hasItems() {
		return myQueue.size()>0;
	}
	
	public String getResultJSONByName(String name) {
		if(name!=null && myQueue.containsKey(name)) {
			Analyzer a=myQueue.get(name);
			return a.getResultsJSON();
		} else {
			return "[]";
		}
	}
	
}
