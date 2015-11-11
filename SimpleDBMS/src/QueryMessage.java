
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

//==========================Messages for Project 3===========================
//==========================Messages for Project 3===========================
//==========================Messages for Project 3===========================
//==========================Messages for Project 3===========================

class InsertResult implements QueryMessage
{
	public String toString()
	{
		return "The row is inserted";
	}
}

class InsertDuplicatePrimaryKeyError implements QueryMessage
{
	public String toString()
	{
		return "Insertion has failed: Primary key duplication";
	}
}

class InsertReferentialIntegrityError implements QueryMessage
{
	public String toString()
	{
		return "Insertion has failed: Referential integrity violation";
	}
}

class InsertTypeMismatchError implements QueryMessage
{
	public String toString()
	{
		return "Insertion has failed: Types are not matched";
	}
}

class InsertColumnExistenceError implements QueryMessage
{
	InsertColumnExistenceError(String cn)
	{
		columnName = cn;
	}
	public String columnName="";
	public String toString()
	{
		return "Insertion has failed: \"["+columnName+"]\" does not exist";
	}
}

class InsertColumnNonNullableError implements QueryMessage
{
	InsertColumnNonNullableError(String cn)
	{
		columnName = cn;
	}
	public String columnName="";
	public String toString()
	{
		return "Insertion has failed: \"["+columnName+"]\" is not null";
	}
}

class DeleteResult implements QueryMessage
{
	DeleteResult(int a)
	{
		count = a;
	}
	int count=0;
	
	public String toString()
	{
		return "["+count+"] row(s) are deleted";
	}
}

class DeleteReferentialIntegrityPassed implements QueryMessage
{
	DeleteReferentialIntegrityPassed(int a)
	{
		count = a;
	}
	int count=0;
	
	public String toString()
	{
		return "["+count+"] row(s) are not deleted due to referential integrity";
	}
}

class SelectTableExistenceError implements QueryMessage
{
	SelectTableExistenceError(String cn)
	{
		tableName = cn;
	}
	public String tableName="";
	public String toString()
	{
		return "Selection has failed: \"["+tableName+"]\" does not exist";
	}
}

class SelectColumnResolveError implements QueryMessage
{
	SelectColumnResolveError(String cn)
	{
		columnName = cn;
	}
	public String columnName="";
	public String toString()
	{
		return "Selection has failed: fail to resolve\"["+columnName+"]\"";
	}
}

class WhereIncomparableError implements QueryMessage
{
	public String toString()
	{
		return "Where clause try to compare incomparable values";
	}
}

class WhereTableNotSpecified implements QueryMessage
{
	public String toString()
	{
		return "Where clause try to reference tables which are not specified";
	}
}

class WhereColumnNotExist implements QueryMessage
{
	public String toString()
	{
		return "Where clause try to reference non existing column";
	}
}

class WhereAmbiguousReference implements QueryMessage
{
	public String toString()
	{
		return "Where clause contains ambiguous reference";
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


class PredicateException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 10L;
	QueryMessage mes;
	public PredicateException(QueryMessage a)
	{
		mes = a;
	}
}
