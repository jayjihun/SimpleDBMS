import java.util.Vector;

public interface QueryRequest
{

}


class RefColumn
{
	public Vector<String> refingNames;
	public String tableName;
	public Vector<String> refedNames;
	public RefColumn(Vector<String> refingC, String tName, Vector<String> refedC)
	{
		refingNames = refingC;
		tableName = tName;
		refedNames = refedC;
	}
}

class CreateTableRequest implements QueryRequest
{
	public String tableName;
	public Vector<String> columnNames;
	public Vector<Column> columns;
	public Vector<String> primaryColumns;
	public Vector<RefColumn> referenceColumns;
	public boolean keyAlready;
	public boolean dupKeyError;
	
	public CreateTableRequest()
	{
		referenceColumns = new Vector<RefColumn>(0);
		columnNames = new Vector<String>(0);
		columns = new Vector<Column>(0);
		primaryColumns = new Vector<String>(0);
		keyAlready=false;
		dupKeyError=false;
	}
	public String toString()
	{
		String result = "====CREATE TABLE====\n";
		for(Column col : columns)
			result+=col+"\n";
		return result;		
	}
}

class DropTableRequest implements QueryRequest
{
	Vector<String> colList;
	boolean all;
	public DropTableRequest()
	{
		colList = new Vector<String>(0);
		all = false;
	}
	public String toString()
	{
		String result = "{{{{{{{DROP TABLE}}}}}}}\n";
		if(all)
		{
			result+="ALL!!\n";
			return result;
		}
		for(String colName : colList)
			result+=colName+" ";
		result+="\n";
		return result;	
	}
}

class DescRequest implements QueryRequest
{
	Vector<String> colList;
	boolean all;
	public DescRequest()
	{
		colList = new Vector<String>(0);
		all = false;
	}
	public String toString()
	{
		String result = "{{{{{{{DESC}}}}}}}\n";
		if(all)
		{
			result+="ALL!!\n";
			return result;
		}
		for(String colName : colList)
			result+=colName+" ";
		result+="\n";
		return result;	
	}
}

class InsertRequest implements QueryRequest
{
	String tableName;
	Vector<String> columnNames;
	Vector<String> values;
	InsertRequest()
	{
		tableName = "";
		columnNames = null;
		values = new Vector<String>(0);
	}
}

class DeleteRequest implements QueryRequest
{
	public boolean delAll;
	
	public DeleteRequest()
	{
		delAll = true;
	}
}

class SelectRequest implements QueryRequest
{
	public boolean selectAll;
	public Vector<SelectedColumn> selectedColumnList;
	public Vector<SelectedTable> selectedTableList;
}



