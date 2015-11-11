import java.util.Vector;

public class SelectedColumn
{
	public boolean isTableValid;
	public boolean isAliasValid;
	public String tableName;
	public String columnName;
	public String aliasName;
	
	public SelectedColumn(String columnName,String tableName, String aliasName)
	{
		isTableValid = tableName.length() != 0;
		isAliasValid = aliasName.length() != 0;
		this.columnName = columnName;
		this.tableName = tableName;
		this.aliasName = aliasName;
	}
}

class SelectedTable
{
	public boolean isAliasValid;
	public String tableName;
	public String aliasName;
	
	public SelectedTable(String tableName, String aliasName)
	{
		this.tableName = tableName;
		this.aliasName = aliasName;
		this.isAliasValid = aliasName.length() != 0;
	}
}


class BooleanValueExpression
{
	Vector<BooleanTerm> booleanterms;
	
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException
	{
		ExBoolean result = new ExBoolean(ExBoolean.FALSE);
		for(BooleanTerm bt : booleanterms)
			result = result.or(bt.evaluate(columns, record));
		return result;
	}
	public BooleanValueExpression(Vector<BooleanTerm> bts)
	{
		this.booleanterms=bts;
	}
}

class BooleanTerm
{
	Vector<BooleanFactor> booleanfactors;
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException
	{
		ExBoolean result = new ExBoolean(ExBoolean.TRUE);
		for(BooleanFactor bf : booleanfactors)
			result = result.and(bf.evaluate(columns, record));
		return result;
	}
	
	public BooleanTerm(Vector<BooleanFactor> bfs)
	{
		this.booleanfactors = bfs;
	}
}

class BooleanFactor
{
	public boolean isNot;
	public BooleanTest test;
	
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record)  throws PredicateException
	{
		if(isNot)
			return test.evaluate(columns, record).not();
		else
			return test.evaluate(columns, record);
	}
	
	public BooleanFactor(boolean isNot, BooleanTest test)
	{
		this.isNot = isNot;
		this.test = test;
	}
}

class BooleanTest
{
	public final static int PREDICATE = 1;
	public final static int EXPRESSION = 2;
	int type;
	public Predicate pred;
	public BooleanValueExpression bve;
	
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException
	{
		if(type == PREDICATE)
			return pred.evaluate(columns, record);
		else
			return bve.evaluate(columns, record);
	}
	public BooleanTest(int type, Predicate p, BooleanValueExpression b)
	{
		this.type = type;
		this.pred = p;
		this.bve = b;
	}
}

abstract class Predicate
{
	public abstract ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException;
}

class NullPredicate extends Predicate
{
	public String tableName;
	public String columnName;
	public boolean isTNameValid;
	public boolean isNot;
	public NullPredicate(String tableName, String columnName, boolean istnamv, boolean isNot)
	{
		this.tableName = tableName;
		this.columnName=columnName;
		this.isTNameValid=istnamv;
		this.isNot=isNot;
	}
	
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException
	{
		int i=0;
		for(Column colTry : columns)
		{
			if(colTry.columnName.equals(columnName))
			{
				if(isTNameValid)
					if(!colTry.tableName.equals(tableName))
					{
						i++;
						continue;
					}
						
				//found!!
				break;
			}
			i++;
		}
		
		String value = record.elementAt(i);
		boolean isNull=value.equals("@");
		boolean result = isNot^isNull;
		if(result) return new ExBoolean(ExBoolean.TRUE);
		else return new ExBoolean(ExBoolean.FALSE);
	}
}

class ComparisonPredicate extends Predicate
{
	public CompareOperand lhs;
	public CompareOperand rhs;
	public CompareOperator op;
	
	public ExBoolean evaluate(Vector<Column> columns, Vector<String> record) throws PredicateException
	{
		Value lval,rval;
		lval= lhs.getValue(columns, record);
		rval= rhs.getValue(columns, record);
		if(lval.isNull || rval.isNull)
			return new ExBoolean(ExBoolean.UNKNOWN);
		
		if(!lval.dataType.equals(rval.dataType))
			throw new PredicateException(new WhereIncomparableError());
		
		if(lval.dataType.type==DataType.INT)
		{
			int a = Integer.parseInt(lval.value);
			int b = Integer.parseInt(rval.value);
			
			switch(op.type)
			{
			case CompareOperator.EQ:
				return ExBoolean.fromBool(a==b);
			case CompareOperator.GE:
				return ExBoolean.fromBool(a>=b);
			case CompareOperator.GT:
				return ExBoolean.fromBool(a>b);
			case CompareOperator.LE:
				return ExBoolean.fromBool(a<=b);
			case CompareOperator.LT:
				return ExBoolean.fromBool(a<b);
			case CompareOperator.NE:
				return ExBoolean.fromBool(a!=b);
			default:
				return new ExBoolean(ExBoolean.UNKNOWN);
			}
		}
		int inter = 0;
		switch(op.type)
		{
		case CompareOperator.EQ:
			boolean res = lval.value.equals(rval.value);
			return ExBoolean.fromBool(res);
		case CompareOperator.GE:
			inter = lval.value.compareTo(rval.value);
			return ExBoolean.fromBool(inter>=0);
		case CompareOperator.GT:
			inter = lval.value.compareTo(rval.value);
			return ExBoolean.fromBool(inter>0);
		case CompareOperator.LE:
			inter = lval.value.compareTo(rval.value);
			return ExBoolean.fromBool(inter<=0);
		case CompareOperator.LT:
			inter = lval.value.compareTo(rval.value);
			return ExBoolean.fromBool(inter<0);
		case CompareOperator.NE:
			return ExBoolean.fromBool(!lval.value.equals(rval.value));
		default:
			return new ExBoolean(ExBoolean.UNKNOWN);
		}
			
	}
	public ComparisonPredicate(CompareOperand lhs, CompareOperator op, CompareOperand rhs)
	{
		this.lhs = lhs;
		this.op = op;
		this.rhs = rhs;
	}
}


class CompareOperand
{
	public ComparableValue v;
	public String tName;
	public String cName;
	public boolean isTableNameValid;
	public boolean isComparableValue;
	
	public CompareOperand(ComparableValue v, String tname, String cname, boolean isTV, boolean isCV)
	{
		this.v=v;
		this.tName=tname;
		this.cName=cname;
		this.isTableNameValid=isTV;
		this.isComparableValue=isCV;
	}
	
	public Value getValue(Vector<Column> columns, Vector<String> record) throws PredicateException 
	{
		if(isComparableValue)
			return v;
		/*
		 * in here, we get the Value() of operand.
		 * it can throw 3 Exceptions:
		 * 1. WhereTableNotSpecified : tname does not point any table.
		 * 2. WhereColumnNotExist : cname does not point any table.
		 * 3. WhereAmbiguousReference :
		 */
		
		if(isTableNameValid)
			if(SimpleDBMSParser._getTable(tName)==null)
				throw new PredicateException(new WhereTableNotSpecified());
		
		
		
		int i=0;
		boolean found=false;
		for(Column colTry : columns)
		{
			if(colTry.columnName.equals(cName))
			{
				if(isTableNameValid)
					if(!colTry.tableName.equals(tName))
					{
						i++;
						continue;
					}
				found=true;
				break;
			}
			i++;
		}
		
		if(!found)
			throw new PredicateException(new WhereColumnNotExist());
		boolean found2=false;
		if(!isTableNameValid)
		{
			for(Column colTry : columns)
			{
				if(colTry.columnName.equals(cName))
				{
					if(found2)
						throw new PredicateException(new WhereAmbiguousReference());
					found2=true;
				}
			}
		}
		
		String pureValue = record.elementAt(i);
		if(pureValue.equals("@"))
			return new Value();
		return new Value(pureValue,columns.elementAt(i).dataType);
	}
}

class CompareOperator
{
	int type;
	public final static int LT = 1; // <
	public final static int GT = 2; // >
	public final static int EQ = 3; // =
	public final static int GE = 4; // >=
	public final static int LE = 5; // <=
	public final static int NE = 6; // !=
	
	public CompareOperator(String a)
	{
		if(a.equals("<"))
			type = LT;
		else if(a.equals(">"))
			type = GT;
		else if(a.equals("="))
			type = EQ;
		else if(a.equals(">="))
			type = GE;
		else if(a.equals("<="))
			type = LE;
		else if(a.equals("!="))
			type = NE;
		else
			type =0;
	}
}

class Value
{
	public boolean isNull = false;
	public String value;
	public DataType dataType;
	
	public Value(String val, int type, int length)
	{
		value = val;
		dataType = new DataType(type,length);
		isNull=false;
	}
	
	public Value(String val, DataType dt)
	{
		value = val;
		dataType = dt;
	}
	
	public Value()
	{
		value ="@";
		isNull=true;
		dataType=null;
	}
}

class ComparableValue extends Value
{
	public ComparableValue(String val, int type, int length)
	{
		super(val,type,length);
	}
}

class ExBoolean
{
	int value;
	public final static int FALSE = 0;
	public final static int TRUE = 1;
	public final static int UNKNOWN = 2;
	
	
	public ExBoolean(int ini)
	{
		value = ini;
	}
	
	public boolean convert()
	{
		switch(value)
		{
		case FALSE:
			return false;
		case UNKNOWN:
			return false;
		case TRUE:
			return true;
		default:
			return false;
		}
	}
	
	public ExBoolean and(ExBoolean b)
	{
		switch(value)
		{
		case FALSE:
			return new ExBoolean(FALSE);
		case TRUE:
			return b;
		case UNKNOWN:
			if(b.value == UNKNOWN)
				return new ExBoolean(UNKNOWN);
			else
				return b.and(this);
		default:
			return new ExBoolean(UNKNOWN);				
		}
	}
	
	public ExBoolean or(ExBoolean b)
	{
		switch(value)
		{
		case FALSE:
			return b;
		case TRUE:
			return new ExBoolean(TRUE);
		case UNKNOWN:
			if(b.value == UNKNOWN)
				return new ExBoolean(UNKNOWN);
			else
				return b.and(this);
		default:
			return new ExBoolean(UNKNOWN);				
		}
	}
	
	public ExBoolean not()
	{
		switch(value)
		{
		case FALSE:
			return new ExBoolean(TRUE);
		case UNKNOWN:
			return new ExBoolean(UNKNOWN);
		case TRUE:
			return new ExBoolean(FALSE);
		default:
			return new ExBoolean(UNKNOWN);				
		}
	}
	
	public static ExBoolean fromBool(boolean a)
	{
		return a?new ExBoolean(TRUE):new ExBoolean(FALSE);
	}
	
	public ExBoolean isUnknown()
	{
		if(value == UNKNOWN)
			return new ExBoolean(TRUE);
		return new ExBoolean(FALSE);
	}
}