/*

   Licensed Materials - Property of IBM
   Cloudscape - Package org.apache.derby.iapi.sql
   (C) Copyright IBM Corp. 1997, 2004. All Rights Reserved.
   US Government Users Restricted Rights - Use, duplication or
   disclosure restricted by GSA ADP Schedule Contract with IBM Corp.

 */

package org.apache.derby.iapi.sql;

import org.apache.derby.iapi.error.StandardException;
import org.apache.derby.iapi.sql.dictionary.SchemaDescriptor;
import org.apache.derby.iapi.sql.conn.LanguageConnectionContext;

/**
 * The Statement interface provides a way of giving a statement to the
 * language module, preparing the statement, and executing it. It also
 * provides some support for stored statements. Simple, non-stored,
 * non-parameterized statements can be executed with the execute() method.
 * Parameterized statements must use prepare(). To get the stored query
 * plan for a statement, use get().
 * <p>
 * This interface will have different implementations for the execution-only
 * and compile-and-execute versions of the product. In the execution-only
 * version, some of the methods will do nothing but raise exceptions to
 * indicate that they are not implemented.
 * <p>
 * There is a Statement factory in the Connection interface in the Database
 * module, which uses the one provided in LanguageFactory.
 *
 *	@author Jeff Lichtman
 */
public interface Statement
{
	/**
		IBM Copyright &copy notice.
	*/
	public static final String copyrightNotice = org.apache.derby.iapi.reference.Copyright.SHORT_1997_2004;

	/**
	 * Generates an execution plan without executing it.
	 *
	 * @return A PreparedStatement that allows execution of the execution
	 *	   plan.
	 * @exception StandardException	Thrown if this is an
	 *	   execution-only version of the module (the prepare() method
	 *	   relies on compilation).
	 */
	PreparedStatement	prepare(LanguageConnectionContext lcc) throws StandardException;

	/**
	 * Generates an execution plan given a set of named parameters.
	 * For generating a storable prepared statement (which
	 * has some extensions over a standard prepared statement).
	 *
	 * @param 	compSchema			the compilation schema to use
	 * @param	paramDefaults		Default parameter values to use for
	 *								optimization
	 * @param	spsSchema schema of the stored prepared statement
	 *
	 * @return A Storable PreparedStatement that allows execution of the execution
	 *	   plan.
	 * @exception StandardException	Thrown if this is an
	 *	   execution-only version of the module (the prepare() method
	 *	   relies on compilation).
	 */
	public	PreparedStatement	prepareStorable
	( 
		LanguageConnectionContext lcc,
		PreparedStatement ps, 
		Object[]			paramDefaults,
		SchemaDescriptor	spsSchema,
		boolean	internalSQL
	)
		throws StandardException;

	/**
	 *	Return the SQL string that this statement is for.
	 *
	 *	@return the SQL string this statement is for.
	 */
	String getSource();

	public boolean getUnicode();

}
