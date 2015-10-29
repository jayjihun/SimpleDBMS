
public interface QueryMessage
{
	public String toString();
}

class CreateTableSuccess implements QueryMessage
{
	public String tableName;
	public CreateTableSuccess(String tableName)
	{
		this.tableName = tableName;
	}
	public String toString()
	{
		return "\""+tableName+"\" table is created";
	}
}

class DuplicateColumnDefError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: column definition is duplicated";
	}
}

class DuplicatePrimaryKeyDefError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: primary key definition is duplicated";
	}
}

class ReferenceTypeError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: foreign key references wrong type";
	}
}
class ReferenceNonPrimaryKeyError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: foreign key references non primary key column";
	}
}
class ReferenceColumnExistenceError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: foreign key references non existing column";
	}
}
class ReferenceTableExistenceError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: foreign key references non existing table";
	}
}
class NonExistingColumnDefError implements QueryMessage
{
	String colName;
	public NonExistingColumnDefError(String colName)
	{
		this.colName = colName;
	}
	public String toString()
	{
		return "Create table has failed: \""+colName+"\" does not exists in column definition";
	}
}

class TableExistenceError implements QueryMessage
{
	public String toString()
	{
		return "Create table has failed: table with the same name already exists";
	}
}

class DropSuccess implements QueryMessage
{
	String tableName;
	public DropSuccess(String tableName)
	{
		this.tableName=tableName;
	}
	
	public String toString()
	{
		return "\""+tableName+"\" table is dropped";
	}
}

class DropSuccessAllTables implements QueryMessage
{
	public String toString()
	{
		return "Every table is dropped";
	}
}

class DropReferencedTableError implements QueryMessage
{
	String tableName;
	public DropReferencedTableError(String tableName)
	{
		this.tableName=tableName;
	}
	
	public String toString()
	{
		return "Drop table has failed: \""+tableName+"\" is referenced by other table";
	}
}

class NoSuchTable implements QueryMessage
{
	public String toString()
	{
		return "No such table";
	}
}

class CharLengthError implements QueryMessage
{
	public String toString()
	{
		return "Char length should be > 0";
	}
}

class SyntaxError implements QueryMessage
{
	public String toString()
	{
		return "Syntax error";
	}
}
class Dummy implements QueryMessage
{
	public String toString()
	{
		return "";
	}
}

