/*

   Licensed Materials - Property of IBM
   Cloudscape - Package org.apache.derby.impl.sql.compile
   (C) Copyright IBM Corp. 1999, 2004. All Rights Reserved.
   US Government Users Restricted Rights - Use, duplication or
   disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

 */

package org.apache.derby.impl.sql.compile;

import org.apache.derby.iapi.services.loader.ClassFactory;

import org.apache.derby.iapi.error.StandardException;

import org.apache.derby.iapi.types.DataTypeDescriptor;
import org.apache.derby.iapi.types.DataTypeDescriptor;
import org.apache.derby.iapi.types.DateTimeDataValue;
import org.apache.derby.iapi.types.DataValueFactory;
import org.apache.derby.iapi.types.TypeId;

import org.apache.derby.iapi.sql.compile.TypeCompiler;

import org.apache.derby.iapi.services.sanity.SanityManager;

import java.sql.Types;
import org.apache.derby.iapi.reference.ClassName;

public class TimeTypeCompiler extends BaseTypeCompiler
{
	/**
		IBM Copyright &copy notice.
	*/
	public static final String copyrightNotice = org.apache.derby.iapi.reference.Copyright.SHORT_1999_2004;
	/* TypeCompiler methods */

	/**
	 * Dates are comparable to timestamps and to comparable
	 * user types.
	 *
	 * @param otherType the type of the instance to compare with this type.
	 * @param forEquals True if this is an = or <> comparison, false
	 *					otherwise.
	 * @param cf		A ClassFactory
	 * @return true if otherType is comparable to this type, else false.
	 */
	public boolean comparable(TypeId otherType,
                              boolean forEquals,
                              ClassFactory cf)
	{
		int otherJDBCTypeId = otherType.getJDBCTypeId();

		// Long types cannot be compared
		if (otherType.isLongConcatableTypeId())
			return false;

		if (otherJDBCTypeId == Types.TIME || otherType.isStringTypeId())
			return true;

		TypeCompiler otherTC = getTypeCompiler(otherType);

		/* User types know the rules for what can be compared to them */
		if (otherType.userType())
		{
			return otherTC.comparable(getTypeId(), forEquals, cf);
		}

		return false;
	}

	/**
	 * User types are convertible to other user types only if
	 * (for now) they are the same type and are being used to
	 * implement some JDBC type.  This is sufficient for
	 * date/time types; it may be generalized later for e.g.
	 * comparison of any user type with one of its subtypes.
	 *
	 * @see TypeCompiler#convertible 
	 */
	public boolean convertible(TypeId otherType,
							   boolean forDataTypeFunction)
	{

		if (otherType.isStringTypeId() && 
			(!otherType.isLOBTypeId()) &&
			!otherType.isLongVarcharTypeId())
		{
			return true;
		}


		/*
		** If same type, convert always ok.
		*/
		return (getStoredFormatIdFromTypeId() == 
				otherType.getTypeFormatId());
		   
	}

	/** @see TypeCompiler#compatible */
	public boolean compatible(TypeId otherType)
	{
		return convertible(otherType,false);
	}

	/**
	 * User types are storable into other user types that they
	 * are assignable to. The other type must be a subclass of
	 * this type, or implement this type as one of its interfaces.
	 *
	 * Built-in types are also storable into user types when the built-in
	 * type's corresponding Java type is assignable to the user type.
	 *
	 * @param otherType the type of the instance to store into this type.
	 * @param cf		A ClassFactory
	 * @return true if otherType is storable into this type, else false.
	 */
	public boolean storable(TypeId otherType, ClassFactory cf)
	{
		int	otherJDBCTypeId = otherType.getJDBCTypeId();

		if (otherJDBCTypeId == Types.TIME ||
			(otherJDBCTypeId == Types.CHAR) ||
			(otherJDBCTypeId == Types.VARCHAR))
		{
			return true;
		}

		return cf.getClassInspector().assignableTo(
			   otherType.getCorrespondingJavaTypeName(),
			   "java.sql.Time");
	}

	/** @see TypeCompiler#interfaceName */
	public String interfaceName()
	{
		return ClassName.DateTimeDataValue;
	}
			
	/**
	 * @see TypeCompiler#getCorrespondingPrimitiveTypeName
	 */

	public String getCorrespondingPrimitiveTypeName()
	{
		return "java.sql.Time";
	}

	/**
	 * @see TypeCompiler#getCastToCharWidth
	 */
	public int getCastToCharWidth(DataTypeDescriptor dts)
	{
		return 8;
	}

	public double estimatedMemoryUsage(DataTypeDescriptor dtd)
	{
		return 12.0;
	}

	protected String nullMethodName()
	{
		return "getNullTime";
	}
}
