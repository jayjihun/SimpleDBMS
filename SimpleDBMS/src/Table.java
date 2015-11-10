import java.io.Serializable;
import java.util.Vector;


class AllTables implements Serializable
{
	private static final long serialVersionUID = 2L;
	public Vector<String> tableNames;
	public AllTables()
	{
		tableNames = new Vector<String>(0);
	}
	public void addTable(String tablename)
	{
		tableNames.addElement(tablename);
	}
	public void deleteTable(String tablename)
	{
		tableNames.remove(tablename);
	}
	boolean isIn(String tableName)
	{
		return tableNames.indexOf(tableName) != -1;
	}
	
	public String toString()
	{
		String result="";
		result+="Total # of tables : "+tableNames.size()+"\n";
		if(tableNames.size() !=0 )
			for(String tabName : tableNames)
				result+=tabName + " ";
		result+= "\n";
		return result;
	}	
}

public class Table implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public String tableName;
	public Vector<String> columnNames;
	public Vector<Column> columns;
	public Vector<Vector<String>> records;	
	public Vector<String> refering;
	public Vector<String> refered;
	public Table()
	{
		tableName="";
		columnNames=new Vector<String>(0);
		columns=new Vector<Column>(0);
		records=new Vector<Vector<String>>(0);
		refering = null;
		refered = null;
	}
	
	public Table(String tableName, Vector<Column> column, Vector<String> refering, Vector<String> refered)
	{
		this.tableName=tableName;
		this.columns=column;
		this.refering = refering;
		this.refered = refered;
		this.columnNames = new Vector<String>(0);
		records=new Vector<Vector<String>>(0);
		for(Column col : column)
			columnNames.addElement(col.columnName);
	}
	
	public String toString()
	{
		String result = "";
		result += "table_name\t["+tableName+"]\n";
		result += "column_name\ttype\tnull\tkey\n";
		for(Column col : columns)
			result+= col +"\n";
		
		return result;
	}
	
	public Column getColumn(String colName)
	{
		int index = columnNames.indexOf(colName);
		if(index == -1)
			return null;
		return columns.elementAt(index);
	}
	
	public Vector<Vector<String>> getRecords(Vector<String> colNames)
	{
		//check if all column names are valid.
		for(String colName : colNames)
			if(columnNames.indexOf(colName) == -1)
				return null;
		
		//column names all valid. now make tuples.
		Vector<Vector<String>> result = new Vector<Vector<String>>(0);
		for(Vector<String> record : records)
		{
			Vector<String> outputRecord = new Vector<String>(0);
			for(String colName : colNames)
			{
				int index = columnNames.indexOf(colName);
				String value = record.elementAt(index);
				outputRecord.addElement(value);
			}					
			result.addElement(outputRecord);
		}
		
		return result;
	}
	
	public Table aliasTableName(String alias)
	{
		Vector<Column> columns = new Vector<Column>(0);
		for(Column ori_col : this.columns)
			columns.addElement(new Column(ori_col.columnName,ori_col.dataType,ori_col.nullOk,ori_col.isKey,ori_col.isFor,alias));

		Table result = new Table(alias,columns,refering,refered);
		result.records = this.records;
		return result;		
	}
	
	public Vector<String> getRecords(String colName)
	{
		//check if all column names are valid.
		if(columnNames.indexOf(colName) == -1)
			return null;
		
		//column names all valid. now make tuples.
		Vector<String> result = new Vector<String>(0);
		int index = columnNames.indexOf(colName);
		for(Vector<String> record : records)
		{	
				String value = record.elementAt(index);
				result.addElement(value);
		}		
		return result;
	}
	
	public static Table cartesian(Table a, Table b,String bAlias)
	{
		if(a==null || b==null)
			return null;
		Table result = new Table();
		
		if(bAlias.length()==0)
			bAlias = b.tableName;
		
		result.tableName = "@intermediate";
		for(String columnName_A : a.columnNames)
			result.columnNames.addElement(columnName_A);
		for(String columnName_B : b.columnNames)
			result.columnNames.addElement(columnName_B);
		for(Column column_A : a.columns)
			result.columns.addElement(column_A);
		for(Column column_B : a.columns)
		{
			Column column_B_new = new Column(column_B.columnName, column_B.dataType, column_B.nullOk, column_B.isKey, column_B.isFor,bAlias);
			result.columns.addElement(column_B_new);
		}
		for(Vector<String> record_A : a.records)
		{
			for(Vector<String> record_B : b.records)
			{
				Vector<String> newRecord = (Vector<String>)record_A.clone();
				for(String items : record_B)
					newRecord.addElement(items);
				result.records.addElement(newRecord);
			}				
		}
		return result;
	}
	
	public String recordString()
	{
		String result = "";
		for(String columnName : columnNames)
			result += columnName+" ";
		result+="\n";
		for(Vector<String> record : records)
		{
			for(String item : record)
			{
				result+=item+" ";
			}
			result+="\n";
		}
		result+="\n";
		return result;
	}
}

class DataType implements Serializable
{
	private static final long serialVersionUID = 6L;
	public static final int WRONG = 0;
	public static final int INT = 1;
	public static final int CHAR = 2;
	public static final int DATE = 3;
	
	public int type;
	public int length;//for char type

	
	public DataType(int type, int length)
	{
		this.type = type;
		this.length = length;
	}
	
	public boolean equals(Object t)
	{
		if(!(t instanceof DataType))
			return false;
		DataType tmp = (DataType)t;
		return this.type == tmp.type;
		/*
		
		DataType tmp = (DataType)t;
		if(t == this)
			return true;
		if(type != tmp.type)
			return false;
		if(type ==CHAR && (length != tmp.length))
			return false;
		return true;
		*/		
	}
	public String toString()
	{
		switch(type)
		{
		case WRONG:
			return "wrong";
		case INT:
			return "int";
		case DATE:
			return "date";
		case CHAR:
			return "char("+length+")";
		default:
			return "wrong";
		}
	}
}

class Column implements Serializable
{
	private static final long serialVersionUID = 5L;
	String tableName;
	String columnName;
	DataType dataType;
	boolean nullOk;
	boolean isKey;
	boolean isFor;
	String referedTName;
	String referedCName;
	
	public Column(String columnName, DataType dataType, boolean nullOk, boolean isKey, boolean isFor,String tableName)	
	{
		this.columnName = columnName;
		this.dataType = dataType;
		this.nullOk = nullOk;
		this.isKey = isKey;
		this.isFor = isFor;
		this.referedTName="";
		this.referedCName="";
		this.tableName = tableName;
	}
	public boolean isLenOk()
	{
		if(dataType.type != DataType.CHAR)
			return true;
		if(dataType.length>0)
			return true;
		return false;
	}
	public String toString()
	{
		String result = "";
		result+= columnName+"\t\t"+dataType+"\t";
		result+= nullOk?"Y\t":"N\t";
		result+= isKey?"PRI":"";
		result+= isFor?"FOR":"";
		return result;
	}
}