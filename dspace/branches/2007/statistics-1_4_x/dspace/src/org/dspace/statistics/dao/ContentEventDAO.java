package org.dspace.statistics.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dspace.core.Context;
import org.dspace.statistics.event.ContentEvent;
import org.dspace.statistics.event.LogEvent;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRow;
import org.dspace.storage.rdbms.TableRowIterator;

public class ContentEventDAO implements StatisticsDAO {

	private static Logger log = Logger.getLogger(ContentEventDAO.class);

	private Context context;
	private ArrayList log_columns;
	private ArrayList attributes_columns;

	public ContentEventDAO(Context context) {
		this.context=context;
		this.log_columns = new ArrayList();
		this.log_columns.add("logs_id");
		this.log_columns.add("event_date");
		this.log_columns.add("action");
		this.log_columns.add("ip");
		this.attributes_columns = new ArrayList();
		this.attributes_columns.add("attributes_id");
		this.attributes_columns.add("param");
		this.attributes_columns.add("value");
		this.attributes_columns.add("logs_id");
	}

	public boolean commit(LogEvent logEvent) throws StatisticsDAOException {
		TableRow row;
		Hashtable attributes;
		Enumeration enumeration;
		String param,value;
		int logID;

		attributes=logEvent.getAttributes();
		enumeration=attributes.keys();

		try {
				//Creation of the entry in logs table
				row = DatabaseManager.create(context, "logs");
				row.setColumn("event_date", new Date());
		        row.setColumn("action", logEvent.getType());
		        row.setColumn("ip", (String)attributes.get("ip"));

		        DatabaseManager.update(context, row);
		        logID=row.getIntColumn("logs_id");
		        //Creation of the entry in logs table

		        while (enumeration.hasMoreElements()) {
		        	row = DatabaseManager.create(context, "logs_attributes");
		        	param=(String)enumeration.nextElement();
		        	value=(String)attributes.get(param);
		        	row.setColumn("param", param);
		        	row.setColumn("value", value);
		        	row.setColumn("logs_id",logID);
		        	DatabaseManager.update(context, row);
		        }
		        context.commit();
		        return true;
		} catch (SQLException e) {
			log.error("caught exception: ", e);
			throw new StatisticsDAOException(e);
		}
	}

	/**
	 *
	 * This method retrieves the LogEvent from database, using as parameters the action
	 * and the attributes.
	 * The int parameter "range" specifies the date range of retrieved events
	 * If the boolean parameter date is true, also the timestamp of the event
	 * is retrieved.
	 *
	 */

	public LogEvent[] find(String action, String attributes, boolean date, int range) throws StatisticsDAOException {
		LogEvent resultEvent;
		Object[] params;
		try {
			String sqlLogs="";
			String sqlLogsAttributes="";

			//Date attribute
			if (date)
				sqlLogs="SELECT logs_id,event_date FROM logs WHERE action = ? ";
			else
				sqlLogs="SELECT logs_id FROM logs WHERE action = ? ";

			//Time range
			if (range>=0) {
				GregorianCalendar calendarStart=new GregorianCalendar();
				GregorianCalendar calendarEnd=new GregorianCalendar();

				int start=(-range);
				int end=(-range)+1;

				calendarStart.add(Calendar.DAY_OF_MONTH, start);
				calendarEnd.add(Calendar.DAY_OF_MONTH, end);

				sqlLogs+=" AND event_date>= ? AND event_date <= ?";

				params = new Object[3];
				params[0]=new String(action);
				params[1]=new java.sql.Date(calendarStart.getTimeInMillis());
				params[2]=new java.sql.Date(calendarEnd.getTimeInMillis());
			}
			else {
				params = new Object[1];
				params[0]=new String(action);
			}

			StringTokenizer tokenizer=new StringTokenizer(attributes,",");
			Vector vectorAttr=new Vector();
			String tempString="";

			while(tokenizer.hasMoreTokens()) {
				tempString=tokenizer.nextToken();
				tempString=tempString.trim();
				vectorAttr.add(tempString);
			}

			TableRowIterator iterator,iteratorAttr;
			TableRow tableRow,tableRowAttr;

			iterator = DatabaseManager.query(context, sqlLogs, params);
			Vector logEventVector=new Vector();

			while(iterator.hasNext()) {
				resultEvent=new LogEvent();
				tableRow=iterator.next();
				resultEvent.setId(tableRow.getIntColumn("logs_id"));
				if (date)
					resultEvent.setAttribute("date",tableRow.getDateColumn("event_date").toString());

				//SELECT REQUESTED ATTRIBUTES FOR THE EVENT FOUND
				sqlLogsAttributes="SELECT param,value FROM logs_attributes WHERE logs_id='"+resultEvent.getId()+"'";
				iteratorAttr = DatabaseManager.query(context, sqlLogsAttributes);

				while(iteratorAttr.hasNext()) {
					tableRowAttr=iteratorAttr.next();
					if (vectorAttr.contains(tableRowAttr.getStringColumn("param")))
						resultEvent.setAttribute(tableRowAttr.getStringColumn("param"), tableRowAttr.getStringColumn("value"));
				}
				iteratorAttr.close();
				logEventVector.add(resultEvent);
			}
			iterator.close();
			log.info("Items found "+logEventVector.size());
			LogEvent[] results=new LogEvent[logEventVector.size()];
			for(int i=0;i<logEventVector.size();i++)
				results[i]=(LogEvent)logEventVector.elementAt(i);

			return results;
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}
	}

	/**
	 *
	 * This method retrieves the LogEvent from database, using as parameters the action
	 * and the attribute
	 *
	 */

	public LogEvent[] find(String action, String attribute) {
		try {
			String sqlLogs="";
			String sqlLogsAttributes="";
			LogEvent resultEvent;
			TableRowIterator iterator,iteratorAttr;
			TableRow tableRow,tableRowAttr;

			if (action.equals("ALL"))
				sqlLogs="SELECT logs_id,action FROM logs where (action='ITEM_VIEW' or action='COLLECTION_VIEW' or action='COMMUNITY_VIEW' or action='BITSTREAM_VIEW') ";
			else
				sqlLogs="SELECT logs_id,action FROM logs where action='"+action+"'";

			iterator = DatabaseManager.query(context, sqlLogs);
			Vector logEventVector=new Vector();
			boolean paramFound;

			while(iterator.hasNext()) {
				resultEvent=new LogEvent();
				tableRow=iterator.next();
				resultEvent.setId(tableRow.getIntColumn("logs_id"));
				if (tableRow.getStringColumn("action").equals("ITEM_VIEW"))
					resultEvent.setType(ContentEvent.ITEM_VIEW);
				else if (tableRow.getStringColumn("action").equals("COLLECTION_VIEW"))
					resultEvent.setType(ContentEvent.COLLECTION_VIEW);
				else if (tableRow.getStringColumn("action").equals("COMMUNITY_VIEW"))
					resultEvent.setType(ContentEvent.COMMUNITY_VIEW);
				else if (tableRow.getStringColumn("action").equals("BITSTREAM_VIEW"))
					resultEvent.setType(ContentEvent.BITSTREAM_VIEW);

				//SELECT REQUESTED ATTRIBUTE FOR THE EVENT FOUND
				sqlLogsAttributes="SELECT param,value FROM logs_attributes WHERE logs_id='"+resultEvent.getId()+"'";
				iteratorAttr = DatabaseManager.query(context, sqlLogsAttributes);

				paramFound=false;

				while(iteratorAttr.hasNext()) {
					tableRowAttr=iteratorAttr.next();
					if (attribute.equals(tableRowAttr.getStringColumn("param"))) {
						resultEvent.setAttribute(tableRowAttr.getStringColumn("param"), tableRowAttr.getStringColumn("value"));
						paramFound=true;
					}
				}
				iteratorAttr.close();
				if (paramFound)
					logEventVector.add(resultEvent);
			}
			iterator.close();
			log.info("Found items "+logEventVector.size());
			LogEvent[] results=new LogEvent[logEventVector.size()];
			for(int i=0;i<logEventVector.size();i++)
				results[i]=(LogEvent)logEventVector.elementAt(i);

			return results;
		} catch (Exception e) {
			log.error(e.toString());
			return null;
		}
	}

	/**
	 *
	 * This method retrieves the LogEvent from database, using as parameters the action, the attributes
	 * and the date range
	 *
	 */

	public LogEvent[] find(int range, String attributes, String action) throws SQLException {
		String sqlLogs="";
		String sqlLogsAttributes="";
		Object params[] = new Object[2];
		Vector vectorAttr=new Vector();
		LogEvent resultEvent;

		if (action.equals("ALL"))
			sqlLogs="SELECT logs_id,action FROM logs where (action='ITEM_VIEW' or action='COLLECTION_VIEW' or action='COMMUNITY_VIEW' or action='BITSTREAM_VIEW') ";
		else
			sqlLogs="SELECT logs_id,action FROM logs where action='"+action+"'";

		//Time range
		if ((range>=0)&&(action.equals("ALL"))) {
			GregorianCalendar calendarEnd=new GregorianCalendar();
			GregorianCalendar calendarStart=new GregorianCalendar();

			int end=(-range)+1;
			int start=(-range);

			calendarEnd.add(Calendar.DAY_OF_MONTH, end);
			calendarStart.add(Calendar.DAY_OF_MONTH, start);

			sqlLogs+=" AND event_date>= ? AND event_date <= ?";
			params[0]=new java.sql.Date(calendarStart.getTimeInMillis());
			params[1]=new java.sql.Date(calendarEnd.getTimeInMillis());
		}
		else
			params=null;

		if (attributes!=null) {
			StringTokenizer tokenizer=new StringTokenizer(attributes,",");
			vectorAttr=new Vector();
			String tempString="";

			while(tokenizer.hasMoreTokens()) {
				tempString=tokenizer.nextToken();
				tempString=tempString.trim();
				vectorAttr.add(tempString);
			}
		}

		TableRowIterator iterator,iteratorAttr;
		TableRow tableRow,tableRowAttr;

		if (params==null)
			iterator = DatabaseManager.query(context, sqlLogs);
		else
			iterator = DatabaseManager.query(context, sqlLogs, params);

		Vector logEventVector=new Vector();

		while(iterator.hasNext()) {
			resultEvent=new LogEvent();
			tableRow=iterator.next();
			resultEvent.setId(tableRow.getIntColumn("logs_id"));
			if (tableRow.getStringColumn("action").equals("ITEM_VIEW"))
				resultEvent.setType(ContentEvent.ITEM_VIEW);
			else if (tableRow.getStringColumn("action").equals("COLLECTION_VIEW"))
				resultEvent.setType(ContentEvent.COLLECTION_VIEW);
			else if (tableRow.getStringColumn("action").equals("COMMUNITY_VIEW"))
				resultEvent.setType(ContentEvent.COMMUNITY_VIEW);
			else if (tableRow.getStringColumn("action").equals("BITSTREAM_VIEW"))
				resultEvent.setType(ContentEvent.BITSTREAM_VIEW);

			if (attributes!=null) {
				//SELECT REQUESTED ATTRIBUTES FOR THE EVENT FOUND
				sqlLogsAttributes="SELECT param,value FROM logs_attributes WHERE logs_id='"+resultEvent.getId()+"'";
				iteratorAttr = DatabaseManager.query(context, sqlLogsAttributes);

				while(iteratorAttr.hasNext()) {
					tableRowAttr=iteratorAttr.next();
					if (vectorAttr.contains(tableRowAttr.getStringColumn("param")))
						resultEvent.setAttribute(tableRowAttr.getStringColumn("param"), tableRowAttr.getStringColumn("value"));
				}
				iteratorAttr.close();
			}

			logEventVector.add(resultEvent);
		}
		iterator.close();
		log.info("Items found "+logEventVector.size());
		LogEvent[] results=new LogEvent[logEventVector.size()];
		for(int i=0;i<logEventVector.size();i++)
			results[i]=(LogEvent)logEventVector.elementAt(i);

		return results;
	}

	public LogEvent create() throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean delete(LogEvent pi) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return false;
	}

	public LogEvent find(int id) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public LogEvent[] findAll() throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getID() throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean setID(int id) throws StatisticsDAOException {
		// TODO Auto-generated method stub
		return false;
	}

}
