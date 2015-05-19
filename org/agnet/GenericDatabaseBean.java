package org.agnet;

import java.util.*;
import java.sql.*;
import java.io.FileInputStream;
import javax.servlet.http.*;
import javax.servlet.*;

import com.oreilly.servlet.*;



/** UserBean object that can be mirrored as a User database object
 *
 * @author Aaryn Olsson, P2P project, Ag Networking Lab, University of Arizona
 */
 public class GenericDatabaseBean implements java.io.Serializable {

	/* Member Variables */


	private Hashtable myFields;
	private Hashtable myErrors;
	private Hashtable myAttributes;

// How should we store 1-to-many relationships for other tables??

	// Each foreign key in the table is a key in this hash to a generic databaseBean
	// i.e., viruses, symptoms, images...
	private Hashtable relatedTables;

	private String Table_Name;
	private String Primary_Key;
	private String Primary_Key_Value;
	private Connection conn;
	private ServletContext context;

	private String[] related_tables;

	private Hashtable temphash;
	private Hashtable relatedBeans;

	private Hashtable relatedBeansList; // A hashtable or ArrayLists.
					//The ArrayList will be a set of basic Elements (Strings) corresponding to the key to a generic databasebean
	private Hashtable relatedBeanPosition; // A hashtable of current cursor positions for each one-to-many relatedBeans

	private ArrayList oneToManyTables;
	private Hashtable relatedManyTables;

	private boolean record_exists;

	private String error;
	private Exception exception;


	/* Constructor */
/**************************************************/
	public GenericDatabaseBean() {
		/* Initialize Properties */

		myFields=new Hashtable();
		myErrors=new Hashtable();
		myAttributes=new Hashtable();

		relatedTables=new Hashtable();
		relatedBeans=new Hashtable();
		relatedBeansList=new Hashtable();
		relatedManyTables=new Hashtable();
		relatedBeanPosition=new Hashtable();

		Table_Name=new String();
		Primary_Key=new String();
		Primary_Key_Value=new String();

		error=new String("");
//		exception=new Exception();

		record_exists=false;
	}

/**************************************************/
	public void addRelatedTable(String key, Hashtable h){
		key=key.toUpperCase();
		relatedTables.put(key,(Object)h);
//		context.log("addrelated "+key);
		DatabaseField df=(DatabaseField)myFields.get(key);
		df.setForeignKey(true);
	}

/**************************************************/
	public void addRelatedManyTable(String key, Hashtable h){
		key=key.toUpperCase();
		relatedManyTables.put(key,(Object)h);
	}

/**
 * populate member variables of a user from Form input
 * given an HttpServletRequest
 *
 * @param _req - an MultipartRequest object
 *
 *************************************************/
	public void populateFromParams(MultipartRequest _mreq) {
		Enumeration e = _mreq.getParameterNames();
		String field;
		String[] values;
		DatabaseField df;
		while (e.hasMoreElements()){
			// values becomes a String array
			field=(String)e.nextElement();
			values=_mreq.getParameterValues(field);
			if (relatedManyTables.containsKey(field.toUpperCase())){ // then this is a multi
				// for example, viruses, images, symptoms
				for (int i=0;i<values.length;i++){
//					HashTable t
//					GenericDatabaseBean gdb=
// viruses, images, symptoms.... oh my
// unfinished part
				}
			} else {
//				context.log("field: "+field+" value: "+values[0]);
				df=(DatabaseField)myFields.get(field.toUpperCase());
				if (df != null){
					df.setData(values[0]);
					df.setChanged(true);
//					context.log("set data for "+field+" to "+df.getData().toString());
				} else {
					context.log("Databasefield is null from form parameter: "+field);
					context.log("...Often a case problem");
				}
			}
		}
		// Populate bean properties from request parameters
		//setLastName(_mreq.getParameter("last_name"));
	}

/**************************************************/
	public void populateFromParams(HttpServletRequest _req) {
		Enumeration e = _req.getParameterNames();
		String field;
		String upper_field;
		String[] values;
		DatabaseField df;
		while (e.hasMoreElements()){
			// values becomes a String array
			field=((String)e.nextElement());
			values=_req.getParameterValues(field);
			if (relatedManyTables.containsKey(field.toUpperCase())){ // then this is a multi
				// for example, viruses, images, symptoms
				for (int i=0;i<values.length;i++){
//					HashTable t
//					GenericDatabaseBean gdb=
// viruses, images, symptoms.... oh my
// unfinished part
				}
			} else {
//				context.log("field: "+field+" value: "+values[0]);
				df=(DatabaseField)myFields.get(field.toUpperCase());
				if (df != null){
					if (!df.setData(values[0])){
						// error occurred
//						context.log("An error occurred populating "+df.getName()+" -- "+values[0]);
					} else {
						df.setChanged(true);
//						context.log("set data for "+field+" to "+df.getData().toString());
					}
				} else {
//					context.log("Databasefield is null from form parameter: "+field);
				}
			}
		}
		// Populate bean properties from request parameters
		//setLastName(_req.getParameter("last_name"));
	}


/**************************************************/
	public boolean hasErrors(){
		for (Enumeration e=myFields.elements(); e.hasMoreElements(); ){
			DatabaseField df=(DatabaseField)e.nextElement();
//			context.log("Validating "+df.getName()+" ("+df.toString()+"), "+df.getDataType());
			if (!df.isValid()){
//				context.log("df has error: "+df.getName());
				return true;
			}
		}
		return false;
	}

/**************************************************/
	public void setTable(String t){
		Table_Name=t.toUpperCase();
	}

/**************************************************/
	public void setPrimaryKey(String pk){
		Primary_Key=pk.toUpperCase();
	}
/**************************************************/
	public String getPrimaryKey(){
		return Primary_Key;
	}

/**************************************************/
	public void setPrimaryKeyValue(String pk_value){
		Object o=(Object)myFields.get(Primary_Key);
		if (o == null){
			context.log("Bean has not been initialized from metadata");
			return;
		}
		DatabaseField df=(DatabaseField)o;
		df.setData(pk_value);
		Primary_Key_Value=pk_value;
	}
/**************************************************/
	public String getPrimaryKeyValue(){
		return Primary_Key_Value;
	}

/**************************************************/
	public void setConnection(Connection _conn){
		conn=_conn;
	}


/**************************************************/
	public void initializeFromMetadata(){
		String SQL="select * from "+Table_Name;
		try {
			Statement st=conn.createStatement();
			ResultSet rs=st.executeQuery(SQL);
			if (rs.next()){
				ResultSetMetaData rsmd = rs.getMetaData();
				for (int i=1; i<=rsmd.getColumnCount(); i++){
//					context.log("adding "+rsmd.getColumnName(i)+" as a databasefield");
					DatabaseField df=new DatabaseField();
//					context.log("made new Field()");
					df.initializeFromResultSetMetaData(rsmd,i);
//					context.log("initialized Field()");
//					context.log("init... Field name is "+df.getName());
					myFields.put(rsmd.getColumnName(i).toUpperCase(),(Object)df);
//					context.log("added Field() to hash");

// if foreign key, then initialize a new GenericDatabaseBean for it. ? Recursion problem ?
//

						// ?
						// ?
					if (relatedTables.get(df.getName()) != null){
						df.setForeignKey(true);
					}

				}
			}
		} catch (SQLException e){
			context.log("There was a problem initializing from metadata");
			context.log("SQL: "+SQL);
			context.log(e.toString());
			System.out.print(e.toString());
			error="There was a problem initializing from metadata";
			exception=e;
		}
	}


/**
 * populate the GenericDatabaseBean member variables from a database given a connection and username<br>
 * Assumptions:<p>
 * <pre>
 *	1. a table  (Table) exists with some fields
 *  2. Primary_Key is defined
 *  3. Bean has been initialized from metadata
 * </pre>
 * @return true if bean was populated, false otherwise
 */
 	public boolean populateFromDatabase()
	{
		ResultSet rs = null;
		Object o=(Object)myFields.get(Primary_Key);
		DatabaseField df=(DatabaseField)o;

		if (Primary_Key == null){
			context.log("Primary Key is not set");
			return false;
		} if ( df == null){
			context.log("Primary Key DBField is null");
			return false;
		}
		String SQL="select * from "+Table_Name+" where "+Primary_Key+"="+df.getSQLValue();

		try {
			if (conn == null){
				context.log("Database connection is broken");
				return false;
			}
			Statement s = conn.createStatement();
			rs = s.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
//				context.log(SQL);
			if (rs.next()){
				record_exists=true;
				for (int i=1; i<=rsmd.getColumnCount(); i++){
//					context.log("pop DB: " + rsmd.getColumnName(i)+" - "+rs.getString(i));
					o=(Object)myFields.get(rsmd.getColumnName(i).toUpperCase());
					df=(DatabaseField)o;
					o=rs.getObject(i);
					df.setData(o);
					df.setChanged(false);
					if (df.isForeignKey()  && !(df.toString().equals(""))){

//						context.log("foreign_key - "+df.getName());
						// Create a new GenericDatabaseBean for the related information
						GenericDatabaseBean relBean=new GenericDatabaseBean();
						Hashtable related_table_info=(Hashtable)relatedTables.get(df.getName().toUpperCase());
						String related_table=(String)related_table_info.get("related_table_name");
						String related_column=(String)related_table_info.get("related_column_name");

						// initialize the Bean
						relBean.setTable(related_table);
//						context.log("*** related table: "+related_table+" from "+df.getName());
						relBean.setPrimaryKey(related_column);
						relBean.setConnection(conn);
						relBean.initializeFromMetadata();
//						context.log("after iinitialize from metadata related bean");

						/* Populate the Bean */
						relBean.setPrimaryKeyValue(df.toString());
						relBean.populateFromDatabase();
						relatedBeans.put(df.getName().toUpperCase(),relBean);
					}

//					context.log(i+"adding "+rsmd.getColumnName(i));
				} // end of looping through columns

/* Populate ArrayLists for one-to-many fields.  The Arraylist should contain the primary key of
 * the related table
 */
				// iterate through relatedManyTables, a hash of related many-to-many keys
				Enumeration e=relatedManyTables.keys(); // should just have the names of the tables
				while ( e.hasMoreElements() ) {
					String str=(String)e.nextElement();
//					context.log("relatedManyTables: "+str);
					populateList(str);
				}
   				return true;
			} else { // no results

				return false;
			}
		}
		catch (java.sql.SQLException e) {
			context.log("A problem occurred while accessing the database.");
			context.log(e.toString() + "\n" + SQL);
		}
		return false;
	}
/**************************************************/
	public void clearData(){
		DatabaseField df;
		StringBuffer SQL= new StringBuffer();
		boolean at_least_one=false;
		Enumeration en=myFields.elements();
		while (en.hasMoreElements()){
			df = (DatabaseField)en.nextElement();
			// only add the field to the SQL statement if it has changed
			df.setData("");
			df.setError("");
		}
	}

/**************************************************/
	private boolean populateList(String key){
		key=key.toUpperCase();
		Hashtable h=(Hashtable)relatedManyTables.get(key);
		String table=(String)h.get("local_table_name");
		String related_table=(String)h.get("related_table_name");
		String fk=(String)h.get("foreign_key");
		String pk=(String)h.get("primary_key");
//		String pk_val=(String)h.get("primary_key_value");
		String pk_val=Primary_Key_Value;
		String SQL="select "+fk+" from "+table+" where "+pk+"="+pk_val;
		ArrayList BeanList=new ArrayList();
		try {
			ResultSet rs = null;
			if (conn == null){
				context.log("Database connection is broken");
				return false;
			}
//			context.log(SQL);
			Statement s = conn.createStatement();
			rs = s.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			int i=0;
			while (rs.next()){
				GenericDatabaseBean gdb=new GenericDatabaseBean();

		/* Initialize UserBean from table metadata */
		/* In this case, using AARYN.USERS table or view */
				gdb.setTable(related_table);
				gdb.setPrimaryKey(fk);
				gdb.setConnection(conn);
				gdb.initializeFromMetadata();
//				context.log("adding "+rs.getString(1)+" to Beanlist for "+key +" and table "+related_table);

				gdb.setPrimaryKeyValue(rs.getString(1));
				gdb.populateFromDatabase();
				BeanList.add(gdb);
				if (i==0){
//					context.log("adding "+key+" = 0  to relatedBeanPosition");
					relatedBeanPosition.put(key,new Integer(0));
					relatedBeans.put(key,gdb);
				}
				i++;
			}
			relatedBeansList.put(key,BeanList);
		} catch (java.sql.SQLException e) {
			context.log("A problem occurred while accessing the database.");
			context.log(e.toString() + "\n" + SQL);
			this.error="There was a problem accessing the database in populateList()";
			this.exception=e;
			return false;
		}
		return true;

	}

/**************************************************/
	public boolean contains(String key, String value){
		key=key.toUpperCase();
//		value=value.toUpperCase();
		ArrayList beanlist=(ArrayList)relatedBeansList.get(key);
		if (beanlist==null){
			return false;
		}
		if (beanlist.contains(value)){
			return true;
		}
		return false;
	}

/**************************************************/
	private String getUpdateSQL(){
		DatabaseField df;
		StringBuffer SQL= new StringBuffer();
		boolean at_least_one=false;
		Enumeration en=myFields.elements();
		while (en.hasMoreElements()){
			df = (DatabaseField)en.nextElement();
			// only add the field to the SQL statement if it has changed
//			context.log("data: "+df.getData());
			if (df.hasChanged()){
//				context.log("has changed");
				// only add it if it is not null;
				if (df.getData()!=null){
//					context.log("not null");
					// fencepost problem
					if (at_least_one){
						SQL.append(" , "+df.getName()+"="+df.getSQLValue()+" ");
					} else {
						at_least_one=true;
						SQL.append("update "+Table_Name+" set "+df.getName()+"="+df.getSQLValue()+" ");
					}
					at_least_one=true;
				}
			}
		}
		Object o=(Object)myFields.get(Primary_Key);
		df=(DatabaseField)o;

		if (at_least_one){ // then no changes to be made.
			SQL.append(" where "+Primary_Key+"="+df.getSQLValue());
			return SQL.toString();
		}
//		context.log(SQL.toString() + " null");
		return null;
	}

/**************************************************/
	private String getInsertSQL(){
		DatabaseField df;
		StringBuffer Fields= new StringBuffer();
		StringBuffer Values= new StringBuffer();

		boolean at_least_one=false;
		Enumeration en=myFields.elements();
		while (en.hasMoreElements()){
			df = (DatabaseField)en.nextElement();
			// only add the field to the SQL statement if it has changed
			if (df.hasChanged()){
				// only add it if it is not null;
				if (df.getData()!=null){
					// fencepost problem
					if (at_least_one){
						Fields.append(" , "+df.getName());
						Values.append(", "+df.getSQLValue());
					} else {
						at_least_one=true;
						Fields.append(df.getName());
						Values.append(df.getSQLValue());
					}
				}
			}
		}
		if (at_least_one){
			return "Insert into "+Table_Name+" ("+Fields.toString()+") values ("+Values.toString()+")";
		}
		return null;
	}
/**************************************************/
	private PreparedStatement getUpdateStatement(){
		DatabaseField df;
		StringBuffer SQL=new StringBuffer();
		ArrayList values=new ArrayList();
		ArrayList classes=new ArrayList();
		boolean at_least_one=false;
		PreparedStatement st;
		Enumeration en=myFields.elements();
		while (en.hasMoreElements()){
			df = (DatabaseField)en.nextElement();
			// only add the field to the SQL statement if it has changed
			if (df.hasChanged()){
				// only add it if it is not null;
				if (df.getData()!=null){
					// fencepost problem
					if (at_least_one){
						SQL.append(" , "+df.getName()+"=? ");
					} else {
						at_least_one=true;
						SQL.append("update "+Table_Name+" set "+df.getName()+"=?");
					}
					values.add(df.getData());
					classes.add(df.getClass());
					at_least_one=true;
				}
			}
		}
		Object o=(Object)myFields.get(Primary_Key);
		df=(DatabaseField)o;

		if (at_least_one){ // then no changes to be made.
			SQL.append(" where "+Primary_Key+"=?");
			values.add(df.getData());
			classes.add(df.getColumnClassName());
			try {
				st=conn.prepareStatement(SQL.toString());
				for (int i=0; i<values.size(); i++){


					if (classes.get(i).equals("java.math.BigDecimal")){

						st.setBigDecimal(i,(java.math.BigDecimal)values.get(i));
					}
					else if (classes.get(i).equals("java.sql.Timestamp")){
						st.setTimestamp(i,(java.sql.Timestamp)values.get(i));
					}
					else if (classes.get(i).equals("java.lang.String")){
						st.setString(i,(java.lang.String)values.get(i));
					}
				}
				return st;

			} catch (Exception e){
				context.log("Problem creating PreparedStatement in GenericDatabaseBean.java");
				context.log(e.toString());
				context.log("SQL: "+SQL);
				this.error="There was a problem in getUpdateStatement()";
				this.exception=e;
			}
		}
//		context.log(SQL.toString() + " null");
		return null;
	}

/**************************************************/
	private PreparedStatement getInsertStatement(){
		return getUpdateStatement();
	}

 /**************************************************/
	public boolean updateDatabase()
	{
		ResultSet rs = null;
		String SQL;
		if (record_exists){
			SQL=getUpdateSQL();
			context.log("Update SQL: "+SQL);
		} else {
			SQL=getInsertSQL();
			context.log("Insert SQL: "+SQL);
		}
		if (SQL==null){
			context.log("Fields did not change; SQL value is null");
			return false;
		}
		context.log("SQL: "+SQL);
		try {
			if (conn == null){
				context.log("Database connection is broken");
				return false;
			} else {
				Statement s = conn.createStatement();
				s.executeQuery(SQL);
				return true;
			}
		}
		catch (java.sql.SQLException e) {
			context.log("A problem occurred while accessing the database.");
			context.log(e.toString() + "\n" + SQL);
			this.error="There was a problem in updateDatabase()";
			this.exception=e;
			return false;
		}
	}
 /**************************************************/
	public boolean updateDatabase2()
	{
		ResultSet rs = null;
		PreparedStatement st;
		if (record_exists){
			st=getUpdateStatement();
//			context.log("Update SQL: "+SQL);
		} else {
			st=getInsertStatement();
//			context.log("Insert SQL: "+SQL);
		}
		if (st==null){
			context.log("Fields did not change; PreparedStatement is null");
			return false;
		}
		try {
			if (conn == null){
				context.log("Database connection is broken");
				return false;
			} else {
				st.execute();
				return true;
			}
		}
		catch (java.sql.SQLException e) {
			context.log("A problem occurred while accessing the database.");
			context.log(e.toString());
				this.error="There was a problem in updateDatabasebase2()";
				this.exception=e;
			return false;
		}
	}

//**************************************************/
	public boolean isValid(String attrib_name){
		attrib_name=attrib_name.toUpperCase();
		DatabaseField df=(DatabaseField)myFields.get(attrib_name.toUpperCase());
		if (df==null){
			return false;
		}
		if (df.isValid()){
			return true;
		}
		return false;
	}

/**************************************************/
	public String getError(String attrib_name){
		attrib_name=attrib_name.toUpperCase();
		DatabaseField df=(DatabaseField)myFields.get(attrib_name.toUpperCase());
		if (df != null){
			return df.getError();
		}
		return new String("");
	}
/**************************************************/
	public void setError(String attrib_name, String error){
		attrib_name=attrib_name.toUpperCase();
		DatabaseField df=(DatabaseField)myFields.get(attrib_name.toUpperCase());
		if (df != null){
			df.setError(error);
		}
	}

/**************************************************/
/**
 * public boolean setData(String attrib_name, String data)
 *
 * @param attrib_name - a key to the myFields hash table
 * @param data - String data
 *
 * @return true if the operation was successful.  if false, the errors will be in getError(attrib_name) method
 */
	public boolean set(String attrib_name, String data)
	{
		attrib_name=attrib_name.toUpperCase();
		return setData(attrib_name, (Object)data);
	}

/**************************************************/
/**
 * private boolean setData(String attrib_name, Object data)
 *
 * @param attrib_name - a key to the myFields hash table
 * @param data - object
 *
 * @return true if the operation was successful.  if false, the errors will be in getError(attrib_name) method
 */
	private boolean setData(String attrib_name, Object data)
	{
		attrib_name=attrib_name.toUpperCase();
		DatabaseField df=(DatabaseField)myFields.get(attrib_name.toUpperCase());
		if (df == null){
			return false;
		}
		df.setData(data);
		if (!df.isValid()){
			myErrors.put(attrib_name,df.getError());
//			errors=df.getErrors();
			myFields.remove(attrib_name);
			return false;
		}
		return true;
	}

/**************************************************/
	public Object getAttribute(String attrib_name){
		attrib_name=attrib_name.toUpperCase();
		return myAttributes.get(attrib_name.toUpperCase());
	}

/**************************************************/
	public void setAttribute(String attrib_name, Object attrib_data)
	{
		attrib_name=attrib_name.toUpperCase();
		myAttributes.put(attrib_name.toUpperCase(),attrib_data  );
	}

/**************************************************/
/**
 * private Object getData(String attrib_name)
 *
 * @param attrib_name - a key to the myFields hash table
 *
 * @return The data Object associated with given attribute
 */

	private Object getData(String attrib_name){
		attrib_name=attrib_name.toUpperCase();
		Object o=(Object)myFields.get(attrib_name);

		if (o == null){
			context.log("userbean.getData: o is null  for "+attrib_name);
		}
		DatabaseField df=(DatabaseField)o;
//		o=df.getObject(attrib_name);
//		context.log("after getting data ");
		return df.getData();
	}

/**************************************************/
/**
 * public String get(String attrib_name())
 *
 * @param The attribute name to look up data
 * @return The data associated with given attribute as a String
 */
	public String get(String attrib_name){
		attrib_name=attrib_name.toUpperCase();
		Object o=(Object)myFields.get(attrib_name);

		if (o == null){ // return a valid null string rather than 'null'
			o=myAttributes.get(attrib_name);
			if (o==null){
				return new String("");
			} else {
				return o.toString();
			}
		} else {
			try {
//				System.out.print(" getting "+attrib_name+": ");
				DatabaseField df=(DatabaseField)o; // Cast the Object to a DatabaseField
				if (df.getData() == null){ // it may be a valid databaseField, but contain null data
//					context.log("<null>");
					return new String("");
				}
//				context.log(df.getData().toString());
				return df.getData().toString();

			} catch (Exception e){ // send errror to a logfile rather than crashing the application
				context.log("error with "+attrib_name+" : "+e.toString());
				this.error="There was a problem in get("+attrib_name+")";
				this.exception=e;
				return new String("");
			}
		}
	}

/**************************************************/
/*
 * public String get(String foreign key, String attrib_name())
 *
 * @param The attribute name to look up data
 * @return The data associated with given attribute as a String
 */
	public String get(String foreign_key, String attrib_name){
		foreign_key=foreign_key.toUpperCase();
		attrib_name=attrib_name.toUpperCase();
		GenericDatabaseBean relBean=(GenericDatabaseBean)relatedBeans.get(foreign_key);
		if (relBean == null){
			context.log("Null "+foreign_key);
			return new String("");
		}
//		context.log(foreign_key+"."+attrib_name+" found: "+relBean.get(attrib_name));
		return relBean.get(attrib_name);
	}

/**************************************************/
	public boolean contains(String key){
		key=key.toUpperCase();
//		context.log ("Looking for contains "+key);
		if (relatedBeanPosition.containsKey(key)){
			ArrayList alist=(ArrayList)relatedBeansList.get(key);
			if (alist.size()>0){
//				context.log("list for "+key+" is "+alist.size());
				return true;
			} else {
//				context.log("list for "+key+" is "+alist.size());
				return false;
			}
		}
		return false;
	}

/**************************************************/
	public void reset(String key){
		try {
			key=key.toUpperCase();
			if (relatedBeanPosition.containsKey(key)){
				int pos=0;
				ArrayList alist=(ArrayList)relatedBeansList.get(key);
				GenericDatabaseBean gdb=(GenericDatabaseBean)alist.get(pos);
				relatedBeans.put(key,gdb);
				relatedBeanPosition.put(key,new Integer(pos));
			}
		} catch (Exception e){
			this.error="There was a problem in reset("+key+")";
			this.exception=e;
			context.log("Exception in reset: "+e.toString());
		}
	}

/**************************************************/
	public boolean next(String key){
//		context.log("trying next "+key);
		try {
			key=key.toUpperCase();
//			context.log("trying to find position of "+key+" in relatedBeanPosition");
			if (relatedBeanPosition.containsKey(key)){
				int pos=((Integer)relatedBeanPosition.get(key)).intValue();
				ArrayList alist=(ArrayList)relatedBeansList.get(key);
				if (alist.size()<pos){
					return false;
				}
				GenericDatabaseBean gdb=(GenericDatabaseBean)alist.get(pos);
				relatedBeans.put(key,gdb);
				pos++;
				relatedBeanPosition.put(key,new Integer(pos));
//				context.log("good");
				return true;
			} else {
//				context.log("no good");
			}
		}
		catch (Exception e){
			context.log("problem in next: "+e.toString());
			this.error="problem in next";
			this.exception=e;
		}
		return false;
	}

/**************************************************/
	public void setExisting(boolean b){
		record_exists=b;
	}
/**************************************************/
	public void setContext(ServletContext c){
		this.context=c;
	}

	public String getError(){
		return this.error;
	}
	public void setError(String s){
		this.error=s;
	}
	public Exception getException(){
		return this.exception;
	}
	public void setException (Exception e){
		this.exception=e;
	}
}