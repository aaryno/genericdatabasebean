package org.agnet;

import java.sql.*;
import java.util.*;
import java.math.*;
import java.lang.Math;
import org.apache.commons.validator.GenericValidator;

// mirrors java.sql.ResultSetMetadata
public class DatabaseField {


	private String CatalogName;
	private String ColumnClassName;
	private int ColumnDisplaySize;
	private String ColumnLabel;
	private String ColumnName;
	private int ColumnType;
	private String ColumnTypeName;
	private int Precision;
	private int Scale;
	private String SchemaName;
	private String TableName;
	private boolean AutoIncrement;
	private boolean CaseSensitive;
	private int Nullable;
	private Object Data;
	private Object oldData;
	private String SQLValue;

	private String RelatedTable;
	private String RelatedTablePrimaryKey;
	private boolean foreign_key;

	private boolean has_changed; // to track changes to the value as it would differ from the database
	// true means that the value has changed in the Field and the database should be updated to reflect
	// the change.. oldData contains the original Data so revert() will put the oldData back in Data. If
	// data is changed more than once, oldData retains the most recently changed data.

	private String Error; // used to report error about type mismatch, field too long, etc.


	public DatabaseField(){
		CatalogName= new String("");
		ColumnClassName= new String("");
		ColumnDisplaySize=-1;
		ColumnLabel=new String("");
		ColumnName=new String("");
		ColumnType=-1;
		ColumnTypeName=new String("");
		Precision=-1;
		Scale=-1;
		SchemaName=new String("");
		TableName=new String("");
		AutoIncrement=false;
		CaseSensitive=false;
		Nullable=0;
		Data=null;
		Error=new String();
		has_changed=false;
		SQLValue=new String("");
		RelatedTable=new String("");
		RelatedTablePrimaryKey=new String("");
		foreign_key=false;
	}

	public void initializeFromResultSetMetaData(ResultSetMetaData rsmd, int column){
		try {
			CatalogName = rsmd.getCatalogName(column);
			ColumnClassName = rsmd.getColumnClassName(column);
			ColumnDisplaySize = rsmd.getColumnDisplaySize(column);
			ColumnLabel = rsmd.getColumnLabel(column);
			ColumnName = rsmd.getColumnName(column);
			ColumnType = rsmd.getColumnType(column);
			ColumnTypeName = rsmd.getColumnTypeName(column);
			Precision = rsmd.getPrecision(column);
			Scale = rsmd.getScale(column);
			SchemaName = rsmd.getSchemaName(column);
			TableName = rsmd.getTableName(column);
			AutoIncrement = rsmd.isAutoIncrement(column);
			CaseSensitive = rsmd.isCaseSensitive(column);
			Nullable = rsmd.isNullable(column);


//			System.out.println("***");
//			System.out.println("CatalogName: "+rsmd.getCatalogName(column));
//			System.out.println("CatalogClassName: " +rsmd.getColumnClassName(column));
//			System.out.println("ColumnDisplaySize: " +rsmd.getColumnDisplaySize(column));
//			System.out.println("ColumnLabel: " +rsmd.getColumnLabel(column));
//			System.out.println("ColumnName: " +rsmd.getColumnName(column));
//			System.out.println("ColumnType: " +rsmd.getColumnType(column));
//			System.out.println("ColumnTypeName: " +rsmd.getColumnTypeName(column));
//			System.out.println("Precision: " +rsmd.getPrecision(column));
//			System.out.println("Scale: " +rsmd.getScale(column));
//			System.out.println("SchemaName: " +rsmd.getSchemaName(column));
//			System.out.println("TableName: " +rsmd.getTableName(column));

//			System.out.println("AutoIncrement: " +rsmd.isAutoIncrement(column));
//			System.out.println("caseSensitive: " +rsmd.isCaseSensitive(column));
//			System.out.println("Nullable: " +rsmd.isNullable(column));

		} catch (SQLException e){
			System.out.println("ResultSetMetadata error: "+e.toString());
		}
	}
	public boolean isRequired(){
		if (Nullable!=0){
			return false;
		} return true;
	}

	public String getName(){
		return ColumnName;
	}
	public int getMaxLength(){
		return ColumnDisplaySize;
	}
	public boolean isNullable(){
		if (Nullable==0){
			return false;
		} else {
			return true;
		}
	}
	public Object getData(){
//		Class c=Class.forName(ColumnClassName);
		return Data;
	}
	public String toString(){
		if (Data == null){
			return new String("");
		}
		return Data.toString();
	}

	public boolean setData(Object _data){
		// only setData if the data is different than current Data
		if (_data == null||_data.toString().equals("")){
			Data="";
			Error="";
			return true;
		}
		try {
			// only change Data if the new _data is different
			if (!_data.equals(Data)){
				// keep track of whether this is in sync with the original date (useful for syncing with database)
				if (has_changed == false){
					oldData=Data; // make a backup for a revert().
					has_changed=true;
					Error="";
				}
//				System.out.println("Class: "+ColumnClassName+" for "+ColumnName);
				if (ColumnClassName.equals("java.lang.String")){
					Data=(String)_data;
					SQLValue="'"+Data.toString()+"'";
				} else if (ColumnClassName.equals("java.math.BigDecimal")){
					Data=new BigDecimal(_data.toString());
	//				System.out.println("setting data for big decimal: "+getName());
					SQLValue=Data.toString();
				} else if (ColumnClassName.equals("java.sql.Timestamp")){
					Data=(String)_data;
//					SQLValue="to_date('MM-DD-YYYY HH:MM:SS','"+Data+"')";
					SQLValue="sysdate";
					System.out.println("Timestamp SQL: "+SQLValue);
				} else {
					System.err.println("Unhandled class in setData: "+ColumnClassName);
					Data=_data;
					SQLValue=Data.toString();
				}
			}
			Error="";
			// Next, check to see if the value conrforms to the database field lengths and
			// return the appropriate true/false value of whether or not it was valid.
			return isValid();
		} catch (ClassCastException e){
			System.out.println("Unable to cast class: "+e.toString()+ " for "+getName());
			Error="Invalid data input: "+_data.toString();
			return false;
		} catch (NumberFormatException e){
			System.out.println("Not a valid number "+e.toString()+ " for "+getName());
			Error="Not a valid number: "+_data.toString();
			return false;
		}
	}

	public int getPrecision(){
		return Precision;
	}

	public int getScale(){
		return Scale;
	}

	public String getLabel(){
		return ColumnLabel;
	}
	public void setLabel(String _label){
		ColumnLabel=_label;
	}
	public String getDataType(){
		return ColumnTypeName;
	}

	public String getSQLValue(){
		return SQLValue;
	}

	public boolean isValid(){
		boolean code=true;
//		System.out.println("Check: "+Data.getClass().toString()+" Val: "+Data.toString()+" Type: "+ColumnTypeName +" Class:" + ColumnClassName);
		if (Data==null || Data==""){
			if (isRequired()){
				Error="This field is required.";
				System.out.println(getName()+" "+Error);
				return false;
			}
//			System.out.println("field has no Data");
			return true;
		}
		if (ColumnClassName.equals("java.lang.String")){
			if (((String)Data).length()>ColumnDisplaySize){
				code=false;
				Error=new String("Field too long: "+(String)Data+" (only "+ ColumnDisplaySize+" characters allowed in "+ColumnName+ ") ");
//			System.out.println(getName()+" "+Error);
			}
		}
		if (ColumnClassName.equals("java.math.BigDecimal")){
			double val=((BigDecimal)Data).doubleValue();
			double db_max=Math.pow(10.0,Precision*1.0);
			if ( val>db_max ){
				code=false;
				Error=new String("Number is too large. This field should be less than "+db_max+".");
//				System.out.println(getName()+" "+Error);
			}
		}
		if (ColumnClassName.equals("java.sql.Timestamp")){
			if (!GenericValidator.isDate((String)Data, "MM-dd-yyyy",false)){
				code=false;
				Error=new String("Invalid Date: in "+ColumnName+ ": "+(String)Data);
//				System.out.println(getName()+" "+Error);
			}
		}
		return code;
	}
	public void setError(String error){
		Error=error;
	}

	public String getError(){
		return Error;
	}

	public void revert(){
		if (hasChanged()){
			Data=oldData;
		}
	}

	public boolean hasChanged(){
		return has_changed;
	}

	/** to reset the has_changed boolean indicator
	  *
	  * This should be called only when the DatabaseField Data is in synch with the bean data
	  */
	public void setChanged(boolean _changed){
		has_changed=_changed;
	}

	public boolean updateDatabase(Connection _conn,String _schema, String _table)
	{
		try {
			String SQL="update "+_schema+"."+_table+" set "+ColumnName+"="+this.toString()+" where "+ColumnName+"=";
			ResultSet rs;
			Statement s = _conn.createStatement();
			rs = s.executeQuery(SQL);

			return true;
		} catch (java.sql.SQLException e){
			System.out.println("Exception in DatabaseField.updateDatabase: "+e.toString());
			return false;
		}
	}

	public boolean isForeignKey(){
		return foreign_key;
	}
	public void setForeignKey(boolean b){
		foreign_key=b;
	}
	public String getRelatedTable(){
		return RelatedTable;
	}
	public String getRelatedTablePrimaryKey(){
		return RelatedTablePrimaryKey;
	}
	
	public String getColumnClassName(){
		return ColumnClassName;
	}
}
