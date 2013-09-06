/**
 * Sorry about terrible coding...
 */

package com.developi.iconuk13.helper;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

import com.developi.toolbox.DevelopiUtils;
import com.google.code.useragent.UserAgentParser;
import com.ibm.domino.xsp.module.nsf.NotesContext;

public class Analyzer implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String LOGDBNAME=""; // Current Database
//	private static final String LOGVIEWNAME="vReferer3";
	private static final String LOGVIEWNAME="Logs";

	private static final int DEFAULTCOUNT=1000; // if no date given, we will search first 1000 records.
	
	public static final int DRAFT=0;
	public static final int RUNNING=1;
	public static final int DONE=2;
	public static final int ERROR=-1;
	
	private String name;
	private int status=DRAFT; 
	private int elapsedTime=0;
	private float progress=0;
	
	private Calendar dateFrom=null;
	private Calendar dateUntil=null;
	
	private Map<String, Integer> results;
	
	public Analyzer() {
		setName(new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss").format(new Date()));
		results=new TreeMap<String, Integer>();
	}
	
	public Analyzer(String name) {
		this();
		setName(name);
	}

	public Analyzer(Date dateFrom, Date dateUntil) {
		this();
		setDateFrom(dateFrom);
		setDateUntil(dateUntil);
	}
	
	public Analyzer(String name, Date dateFrom, Date dateUntil) {
		this(dateFrom, dateUntil);
		setName(name);
	}
	
	public void setName(String name) {
		if(name!=null && name!="") this.name=name;
	}

	public String getName() {
		return this.name;
	}
	
	public void findUACounts(Session session) {
		
		Database logDb=null;
		View logView=null;
		ViewNavigator viewNav=null;
		DateTime dtUntil=null;
		ViewEntry entry=null;		
		
		long startTicks=System.currentTimeMillis();
		
		try {
			System.out.println("Started searching user agents...");

			setStatus(RUNNING);

			if(LOGDBNAME=="") {
				logDb=session.getDatabase("", getCurrentDatabasePath(), false);
			} else {
				logDb=session.getDatabase("", LOGDBNAME, false);
			}
			
			logView=logDb.getView(LOGVIEWNAME);
			
			logView.setAutoUpdate(false);
			viewNav=logView.createViewNav();

			int count=0;
			int dayCount=0;
			int elapsedDays=0;
			boolean more=true;
			
			if(dateFrom==null) {
				entry=viewNav.getFirst();
			} else {
				DateTime dt=session.createDateTime(dateFrom);
				dt.setAnyTime();

				entry=logView.getEntryByKey(dt, false);
				if(entry!=null) {
					viewNav.gotoEntry(entry);
				} else {
					entry=viewNav.getFirst();
				}

				System.out.println("Looking for log entries from "+dt.getDateOnly());
				
				DevelopiUtils.recycleObject(dt);
				
			}
			
			if(dateUntil!=null) {
				dtUntil=session.createDateTime(dateUntil);
				dtUntil.setAnyTime();				
			}		
				
			if(dateFrom!=null && dateUntil!=null) {
				dayCount=Math.abs(DevelopiUtils.daysBetween(dateUntil.getTime(), dateFrom.getTime()));
				if(dayCount==0) dayCount=1;
			}
			
			
			while(entry!=null && more) {

				if(entry.isDocument()) {
					Document doc=entry.getDocument();
					
					if(dateUntil==null) {
						more=count++ < DEFAULTCOUNT;
						progress=(float)count/DEFAULTCOUNT;
					} else {
						
						DateTime dtLog=(DateTime)entry.getColumnValues().get(0);
						dtLog.setAnyTime();
						
						if(dtUntil.timeDifference(dtLog)>0) {
							System.out.println("Stopped on "+dtLog.getDateOnly());
							more=false;
						}

						if(dateFrom!=null) {
							elapsedDays=Math.abs(DevelopiUtils.daysBetween(dateUntil.getTime(), dtLog.toJavaDate()));
							progress=(1-((float)elapsedDays/dayCount));
						} else {
							progress=(float)count/DEFAULTCOUNT;
						}
						
						DevelopiUtils.recycleObjects(dtLog);
					}


					if(more) {
						
						try {
							UserAgentParser uap=new UserAgentParser(doc.getItemValueString("HTTP_User_Agent"));
//						String uapDef=uap.getBrowserName()+"-"+uap.getBrowserVersion();
							String uapDef=uap.getBrowserName();
							
							int uapCount=1+(results.containsKey(uapDef)?(results.get(uapDef).intValue()):0);
							results.put(uapDef, uapCount);
						} catch (Exception e) {
							System.out.println("OOOPS, UAP failed for agent string: '"+doc.getItemValueString("HTTP_User_Agent")+"'. It was epic! ("+e.getMessage()+")");
						}
					}
				}
					
				ViewEntry tEntry=viewNav.getNext();
				DevelopiUtils.recycleObject(entry);
				entry=tEntry;
			}

			setStatus(DONE);

			elapsedTime=(int) ((System.currentTimeMillis()-startTicks)/1000);
			
			System.out.println("Finished searching user agents...");
			
		} catch(NotesException ne) {
			ne.printStackTrace();
			setStatus(ERROR);
		} finally {
			DevelopiUtils.recycleObjects(logDb, logView, viewNav, dtUntil, entry);
		}
		
	}
		
	public void setDateFrom(Date dateFrom) {
		this.dateFrom=Calendar.getInstance();
		this.dateFrom.setTime(dateFrom);
	}
	
	public void setDateUntil(Date dateUntil) {
		this.dateUntil=Calendar.getInstance();
		this.dateUntil.setTime(dateUntil);
	}
	
	public String getResultsJSON() {
		StringBuffer result=new StringBuffer();
		boolean first=true;
		
		result.append("[");
		
		for (Map.Entry<String, Integer> entry : results.entrySet()) {
			if(first) {
				first=false;
			} else {
				result.append(",");
			}
		    result.append("{");
		    result.append("'browser':'");
		    result.append(entry.getKey());
		    result.append("','count':");
		    result.append(entry.getValue());
		    result.append("}");
		}
		
		result.append("]");
		
		return result.toString();
	}
	
	public int getElapsedTime() {
		return elapsedTime;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}

	public String getStatusText() {
		switch(getStatus()) {
		case DRAFT: return "Draft";
		case RUNNING: return "In Progress";
		case DONE: return "Completed";
		case ERROR: return "Failed";
		}
		return "N/A";
	}
	
	public String getDetail() {
		SimpleDateFormat sdf=new SimpleDateFormat("dd.MM.yyyy");

		if(dateFrom!=null && dateUntil!=null) {
			return "Logs between "+sdf.format(dateFrom.getTime())+" - "+sdf.format(dateUntil.getTime());
		} else {
			String detail="";
			if (dateFrom!=null) {
				detail = "below "+sdf.format(dateFrom.getTime());
			} else if (dateUntil!=null) {
				detail = "above "+sdf.format(dateUntil.getTime());
			} else detail="topmost";
		
			return DEFAULTCOUNT+" items "+detail;
		}
		
	}

	private String getCurrentDatabasePath() {
		NotesContext nc=NotesContext.getCurrent();
		return nc.getModule().getDatabasePath();
	}
	
	public boolean isDraft() { return status==DRAFT; }
	public boolean isRunning() { return status==RUNNING; }
	public boolean isDone() { return status==DONE; }
	public boolean isFaulty() { return status==ERROR; }

	public void setProgress(float progress) {
		this.progress = progress;
	}

	public float getProgress() {
		return progress;
	}

}
