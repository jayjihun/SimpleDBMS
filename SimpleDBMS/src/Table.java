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
	
	public Table(String tableName, Vector<Column> column, Vector<String> refering, Vector<String> refered)
	{
		this.tableName=tableName;
		this.columns=column;
		this.refering = refering;
		this.refered = refered;
		this.columnNames = new Vector<String>(0);
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
		if(t == this)
			return true;
		if(type != tmp.type)
			return false;
		if(type ==CHAR && (length != tmp.length))
			return false;
		return true;		
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
	String columnName;
	DataType dataType;
	boolean nullOk;
	boolean isKey;
	boolean isFor;
	
	public Column(String columnName, DataType dataType, boolean nullOk, boolean isKey, boolean isFor)
	{
		this.columnName = columnName;
		this.dataType = dataType;
		this.nullOk = nullOk;
		this.isKey = isKey;
		this.isFor = isFor;
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