import java.util.Vector;

public interface QueryRequest
{
	public QueryMessage execute(AllTables alltables, Vector<Table> tables);
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
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{
		Vector<String> alltablenames = alltables.tableNames;

		//1. if duplicate columns appear.
		for(String colName : columnNames)
		{
			int index = columnNames.indexOf(colName);
			if(columnNames.indexOf(colName,index+1)!=-1)
				return new DuplicateColumnDefError();
		}


		//2. if duplicate primary key definition appears
		if(dupKeyError)
			return new DuplicatePrimaryKeyDefError();
		for(String priColName : primaryColumns)
		{
			int index = primaryColumns.indexOf(priColName);
			if(primaryColumns.indexOf(priColName,index+1)!=-1)
				return new DuplicateColumnDefError();
		}

		//9. if a table with the same name already exists.
		if(alltablenames.indexOf(tableName)!=-1)
			return new TableExistenceError();

		//7. if primary key column is not defined.
		for(String keyColname : primaryColumns)
			if(columnNames.indexOf(keyColname)==-1)
				return new NonExistingColumnDefError(keyColname);

		//8. if foreign key column is not defined.
		for(RefColumn references : referenceColumns)
			for(String refingColname : references.refingNames)
				if(columnNames.indexOf(refingColname)==-1)
					return new NonExistingColumnDefError(refingColname);

		//6. if foreign key refers non-existing table.
		for(RefColumn references : referenceColumns)
			if(alltablenames.indexOf(references.tableName)==-1)
				return new ReferenceTableExistenceError();

		//5. if foreign key refers non-existing column.
		for(RefColumn references : referenceColumns)
		{
			String referedTableName = references.tableName;
			Table referedTable = SimpleDBMSParser._getTable(referedTableName); 
			for(String refedColname : references.refedNames)
				if(referedTable.columnNames.indexOf(refedColname)==-1)
					return new ReferenceColumnExistenceError();
		}

		//3. if foreign key type and referenced column type differ.
		//3-1. if refed and refing column size are different.
		for(RefColumn references : referenceColumns)
			if(references.refingNames.size() != references.refedNames.size())
				return new ReferenceTypeError();
		//3-2. if refed and refing column type are different.
		for(RefColumn references : referenceColumns)
		{
			String referedTableName = references.tableName;
			Table referedTable = SimpleDBMSParser._getTable(referedTableName); 
			for(String refingColname : references.refingNames)
			{
				int index = references.refingNames.indexOf(refingColname);
				String correspondedColname = references.refedNames.elementAt(index);
				Column referedColumn = null;
				for(Column thecol : referedTable.columns)
					if(thecol.columnName.equals(correspondedColname))
						referedColumn = thecol;

				Column referingColumn = null;
				for(Column thecol : columns)
					if(thecol.columnName.equals(refingColname))
						referingColumn = thecol;

				if(!referedColumn.dataType.equals(referingColumn.dataType))
					return new ReferenceTypeError();
			}
		}

		//4. if foreign key refers non-primary key column.
		for(RefColumn references : referenceColumns)
		{
			String referedTableName = references.tableName;
			Table referedTable = SimpleDBMSParser._getTable(referedTableName); 
			for(String refingColname : references.refingNames)
			{
				int index = references.refingNames.indexOf(refingColname);
				String correspondedColname = references.refedNames.elementAt(index);
				Column referedColumn = null;
				for(Column thecol : referedTable.columns)
					if(thecol.columnName.equals(correspondedColname))
						if(!thecol.isKey)
							return new ReferenceNonPrimaryKeyError();
			}
		}

		//10. if char length < 1
		for(Column col : columns)
			if(!col.isLenOk())
				return new CharLengthError();

		//========================All conditions met. Make a Table.==========================
		//========================All conditions met. Make a Table.==========================
		//========================All conditions met. Make a Table.==========================
		//========================All conditions met. Make a Table.==========================
		//0-1.add not null constraints to all primary columns
		for(String priColName : primaryColumns)
			for(Column col : columns)
				if(col.columnName.equals(priColName))
				{
					col.nullOk=false;  
					col.isKey=true;
				}

		//0-2.mark all foreign key columns.
		for(RefColumn references : referenceColumns)
			for(int i=0; i<references.refingNames.size(); i++)
			{
				String refingColName = references.refingNames.elementAt(i);
				String refedColName = references.refedNames.elementAt(i);
			  	for(Column col : columns)
					if(col.columnName.equals(refingColName))
					{				  
						col.isFor=true;
						col.referedTName = references.tableName;
						col.referedCName = refedColName;
					}
			}
						

		//1. table : create a key-value pair!
		//1-1. make a vector about referencing table names.
		Vector<String> referingTableNames = new Vector<String>(0);
		for(RefColumn refelement : referenceColumns)
			referingTableNames.addElement(refelement.tableName);		  

		//1-2. make a table.
		Table newTable = new Table(tableName,columns,referingTableNames,null);//referencing, new Vector<String >(0));

		//1-3. for all referenced table, add this to their lists.
		for(RefColumn refelement : referenceColumns)
		{
			Table refedTable = SimpleDBMSParser._getTable(refelement.tableName);
			if(refedTable.refered == null)
				refedTable.refered = new Vector<String >(0);
			refedTable.refered.addElement(tableName);
		}

		//2. metadata : add this table to alltables.
		alltables.addTable(tableName);
		tables.addElement(newTable);

		return new CreateTableSuccess(tableName);
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
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{
		QueryMessage result = new Dummy();
		Vector<String > tableNamesList;

		//if to drop all tables.
		if(all)
		{
			tables.removeAllElements();
			alltables.tableNames.removeAllElements();
			return new DropSuccessAllTables();
		}
		//if to drop certain tables only.
		else
		{ 
			tableNamesList = colList;			
			for(String tableName : tableNamesList)
			{
				//if table with such tablename does not exist.
				if(alltables.tableNames.indexOf(tableName)==-1)
				{
					System.out.println(new NoSuchTable());
					continue;
				}

				//start to delete

				//1. check if refered.	
				Table targetTable = SimpleDBMSParser._getTable(tableName);
				if(targetTable.refered!=null)
				{
					//if target table is refered by another table.
					if(targetTable.refered.size() != 0)
					{					  
						System.out.println(new DropReferencedTableError(tableName));
						continue;
					}
				}

				//2. remove this from all refering lists.
				for(String referedTableName : targetTable.refering)
				{
					Table referedTable = SimpleDBMSParser._getTable(referedTableName);
					if(referedTable.refered==null)
						referedTable.refered = new Vector<String>(0);
					referedTable.refered.remove(targetTable);
				}
				tables.remove(targetTable);
				alltables.deleteTable(tableName);
				System.out.println(new DropSuccess(tableName));
			}
		}
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
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{
		QueryMessage result = new Dummy();
		Vector<String > tableNamesList;

		if(all)
			tableNamesList = alltables.tableNames;
		else
			tableNamesList = colList;

		for(String tableName : tableNamesList)
		{
			if(alltables.tableNames.indexOf(tableName)==-1)
			{
				System.out.println(new NoSuchTable());
				continue;
			}
			System.out.println("--------------------------------------------------------");
			System.out.println(SimpleDBMSParser._getTable(tableName));
		}
		System.out.println("--------------------------------------------------------");

		return result;
	}
}

class InsertRequest implements QueryRequest
{
	String tableName;
	Vector<String> columnNames;
	Vector<Value> values;
	public InsertRequest()
	{
		tableName = "";
		columnNames = null;
		values = new Vector<Value>(0);
	}
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{
		//1. check if such table exists
		if(!alltables.isIn(tableName))
			return new NoSuchTable();

		Table targetTable = SimpleDBMSParser._getTable(tableName);

		//if no columnNames input, then use default column name order.
		if(columnNames == null)
			columnNames = targetTable.columnNames;

		//2. check if size of columnNames and values are same
		if(columnNames.size() != values.size())
			return new InsertTypeMismatchError();

		//3. check if such column exists
		for(String columnName : columnNames)
			if(targetTable.getColumn(columnName)==null)
				return new InsertColumnExistenceError(columnName);

		//4. check if type of columns and values are the same
		for(int i=0; i<columnNames.size(); i++)
		{
			String columnName = columnNames.elementAt(i);
			Value value = values.elementAt(i);
			Column column = targetTable.getColumn(columnName);

			//if null, skip type checking.
			if(value.isNull)
				continue;

			if(!value.dataType.equals(column.dataType))
				return new InsertTypeMismatchError();
		}

		//5. check of values follow not null constraint
		for(int i=0; i<columnNames.size(); i++)
		{
			Value value = values.elementAt(i);
			String columnName = columnNames.elementAt(i);
			if(value.isNull)
			{
				Column column = targetTable.getColumn(columnName);
				if(!column.nullOk)
					return new InsertColumnNonNullableError(columnNames.elementAt(i));
			}
		}

		//6. check of values follow primary key constraint
		//7. check of values follow foreign key constraint
		for(int i=0; i<columnNames.size(); i++)
		{
			String columnName = columnNames.elementAt(i);
			Value value = values.elementAt(i);
			String rValue="";
			Column column = targetTable.getColumn(columnName);
			
			//6 : if primary key.
			if(column.isKey)
			{
				//if string, trim '
				if(column.dataType.type == DataType.CHAR)
				{
					rValue = value.value = value.value.substring(1,value.value.length()-1);
					int maxLength = column.dataType.length;
					if(rValue.length()>maxLength)
						rValue = value.value = value.value.substring(0, maxLength);
				}
				else
					rValue = value.value;
					
				//check if this value already exist.
				Vector<String > projection = targetTable.getRecords(columnName);
				if(projection.contains(rValue))
					return new InsertDuplicatePrimaryKeyError();
			}
			
			//7 : if foreign key.
			if(column.isFor)
			{
			  /*
			  if this value is null, skip the test.
			  if not null, test if this value exists in referenced table.
			  */
			  	//if null,
			  	if(value.isNull)
			  		continue;

			  	//if not null,			  
				Table refedTable = SimpleDBMSParser._getTable(column.referedTName);
				Vector<String > projection = refedTable.getRecords(column.referedCName);
				if(!projection.contains(rValue))
					return new InsertReferentialIntegrityError();
			}

		}

		//8. check if not typed columns are nullable.
		for(String columnName : targetTable.columnNames)
		{
			//if not specified column name.
			if(!columnNames.contains(columnName))
			{
				Column column = targetTable.getColumn(columnName);
				if(!column.nullOk)
					return new InsertColumnNonNullableError(columnName);
			}
		}
		//========================All conditions met. Make a Record.==========================
		//========================All conditions met. Make a Record.==========================
		//========================All conditions met. Make a Record.==========================
		//========================All conditions met. Make a Record.==========================
		
		//1. make a record in pre-defined order.
		Vector<String > record = new Vector<String >(0);
		Vector<String > table_columnNames = targetTable.columnNames;

		for(String columnName : table_columnNames)
		{
			int index = columnNames.indexOf(columnName);
			Column column = targetTable.getColumn(columnName);
			String rValue="";
			if(!columnNames.contains(columnName))
				rValue = "@";
			else
			{
				Value value = values.elementAt(index);
				rValue = value.value;
			}
			//append to the record.
			record.addElement(rValue);
		}
		
		//2. append the record to the table
		targetTable.records.addElement(record);
		return new InsertResult();
	}
}

class SelectRequest implements QueryRequest
{
	public boolean columnAll;
	public boolean predAll;
	public Vector<SelectedColumn> selectedColumnList;
	public Vector<SelectedTable> selectedTableList;
	public BooleanValueExpression bve;
	
	public SelectRequest()
	{
		columnAll=false;
		predAll=false;		
		selectedColumnList=null;
		selectedTableList=null;
		bve =null;
	}
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{		
		Vector<String > selectedTableNamespace = new Vector<String >(0);
		//1. if table does not exist
		for(SelectedTable stable : selectedTableList)
		{
			String selectedTableName = stable.tableName;
			if(!alltables.isIn(selectedTableName))
				return new SelectTableExistenceError(selectedTableName);
			selectedTableNamespace.addElement(selectedTableName);
		}

		if(!columnAll)
		{		
			String newName="";
			//2. if not select ALL but column resolve error
			//2-1. Column names should not duplicate
			for(SelectedColumn scolumn1 : selectedColumnList)
			{
				for(SelectedColumn scolumn2 : selectedColumnList)
				{
				  	if(scolumn1 == scolumn2)
				  		continue;
					if(scolumn1.columnName.equals(scolumn2.columnName))
					{
						if(!scolumn1.isTableValid || !scolumn2.isTableValid)
							return new SelectColumnResolveError(scolumn1.columnName);
						if(scolumn1.tableName.equals(scolumn2.tableName))
							return new SelectColumnResolveError(scolumn1.columnName);						
					}
				}
			}
			
			//2-2. Column name must be able to specify 1 column.
			Vector<String > columnNamespace = new Vector<String >(0);
			for(SelectedTable stable : selectedTableList)
			{
				String selectedTableName = stable.tableName;
				Table selectedTable = SimpleDBMSParser._getTable(selectedTableName);
				for(String selectedTable_ColumnName : selectedTable.columnNames)
				{
				  	newName = selectedTable_ColumnName;
				  	columnNamespace.addElement(newName);
				}
			}
	
			//2-2a. Selected column name must exist.
			//2-2b. Selected column must be able to specify 1 column.
			for(SelectedColumn scolumn : selectedColumnList)
			{
			  	String scolumnName = scolumn.columnName;
			  	int index = columnNamespace.indexOf(scolumnName);
			  	if(index == -1)
			  		return new SelectColumnResolveError(scolumnName);
			  	if(index == columnNamespace.lastIndexOf(scolumnName))
			  		continue;
			  	if(!scolumn.isTableValid)
			  		return new SelectColumnResolveError(scolumnName);
			  	if(!selectedTableNamespace.contains(scolumnName))
			  		return new SelectColumnResolveError(scolumnName);
			}
	
			//2-3. alias should not duplicate any other alias.
			//2-4. alias should not duplicate any other original namespace.
			for(SelectedColumn scolumn : selectedColumnList)
			{
			  	if(scolumn.isAliasValid)
			  	{
			  	 	String aliasName = scolumn.aliasName;
			  	 	if(columnNamespace.contains(aliasName))
			  	 		return new SelectColumnResolveError(aliasName);
					columnNamespace.addElement(aliasName);		  	 	
			  	}
			}
		}
		Table interTable = null;
		for(SelectedTable sTable : selectedTableList)
		{
		  	Table newTable = SimpleDBMSParser._getTable(sTable.tableName);
			if(interTable == null)
			{
				if(sTable.isAliasValid)
					interTable = newTable.aliasTableName(sTable.aliasName);
				else
					interTable = newTable;
			  	
			  	continue;
			}
			
			String alias="";
			if(sTable.isAliasValid)
				alias = sTable.aliasName;
			interTable = Table.cartesian(interTable, newTable,alias);				
		}
		//----------------------------intermediate calculation completed-------------------
		//----------------------------intermediate calculation completed-------------------
		//----------------------------intermediate calculation completed-------------------
		//----------------------------intermediate calculation completed-------------------
		
		//select those who satisfy where clause.
		Vector<Vector<String>> selectedRecords;
		if(predAll)
		{
			selectedRecords = interTable.records;
		}
		else
		{
			selectedRecords = new Vector<Vector<String>>(0);
			for(Vector<String> record : interTable.records)
			{
				try
				{
					//checking if the predicate is valid is done while Comparing.	
					ExBoolean exResult = bve.evaluate(interTable.columns, record);
					boolean result = exResult.convert();
					if(result)
						selectedRecords.addElement(record);
				}
				catch(PredicateException e)
				{
					QueryMessage result = e.mes;
					return result;
				}				
			}
		}
		
		//----------------------------predicate applying completed-------------------
		//----------------------------predicate applying completed-------------------
		//----------------------------predicate applying completed-------------------
		//----------------------------predicate applying completed-------------------		
		Vector<String> colNames = interTable.columnNames;
		Vector<Vector<String>> finalRecords;
		Vector<String> finalColumns;
		//project items.
		if(!columnAll)
		{			
			Vector<Integer> indexList = new Vector<Integer>(0);
			for(SelectedColumn sColumn : selectedColumnList)
			{
				String sColumnName = sColumn.columnName;
				if(sColumn.isAliasValid)
					sColumnName = sColumn.aliasName;
				indexList.addElement(colNames.indexOf(sColumnName));
			}
			
			finalRecords = new Vector<Vector<String>>(0);
			finalColumns = new Vector<String>(0);
			for(Vector<String> record:selectedRecords)
			{
				Vector<String> oneFinalRecord = new Vector<String>(0);
				for(int index : indexList)
					oneFinalRecord.addElement(record.elementAt(index));
				finalRecords.addElement(oneFinalRecord);
			}
			for(int index : indexList)
				finalColumns.addElement(colNames.elementAt(index));
		}
		else
		{
			finalRecords = selectedRecords;
			finalColumns = colNames;
		}
		
		//----------------------------projection completed-------------------
		//----------------------------projection completed-------------------
		//----------------------------projection completed-------------------
		//----------------------------projection completed-------------------	
		
		//print it!!!
		String result = "";
		for(String columnName : finalColumns)
			result+=columnName+"\t";
		result+="\n";
		for(Vector<String> record : finalRecords)
		{
			for(String item : record)
			{
				if(item.equals("@"))
					item = "null";
				result+=item+"\t";
			}
			result+="\n";
		}
		result+="\n";
		System.out.println(result);
		return new Dummy();
	}
}


class DeleteRequest implements QueryRequest
{
	public boolean delAll;
	public String tname;
	public BooleanValueExpression bve;
	
	public DeleteRequest()
	{
		delAll = true;
		tname = "";
		bve = null;
	}
	
	
	public QueryMessage execute(AllTables alltables, Vector<Table> tables)
	{
		Table targetTable = SimpleDBMSParser._getTable(tname);
		
		//1. if no such table exists.
		if(targetTable == null)
			return new NoSuchTable();
		
		Vector<Vector<String>> records = targetTable.records;
		for(Vector<String> record : records)
		{	
			boolean delFinal = false;
			boolean shouldIEvalPredicate = true;
			if(delAll)
			{
				delFinal = true;
			}
			else
			{
				//1. does any table refer to me????
				boolean amIRefered = (targetTable.refered.size() == 0);
				if(amIRefered)
				{
					//2. is all columns refering me nullable?????
					boolean isAllReferingsNullable = true;
					for(String referingTableName : targetTable.refered)
					{
						Table referingTable = SimpleDBMSParser._getTable(referingTableName));
						
					}
				}
				
	
				
				if(shouldIEvalPredicate)
				{
					ExBoolean toDel = bve.evaluate(targetTable.columns,record);
					delFinal = toDel.convert();
				}
				
			}
			if(delFinal)
				records.remove(record);			
		}
		
		return null;
	}
}

