/*

Derby - Class org.apache.derbyTesting.functionTests.tests.upgradeTests.Changes10_3

Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package org.apache.derbyTesting.functionTests.tests.upgradeTests;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Upgrade test cases for changes made in 10.3.
 * If the old version is 10.3 or later then these tests
 * will not be run.

 * <BR>
 * 10.3 Upgrade issues
 */
public class Changes10_3 extends UpgradeChange {
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Upgrade changes for 10.3");
        
        suite.addTestSuite(Changes10_3.class);
        
        return suite;
    }

    public Changes10_3(String name) {
        super(name);
    }
    

    
    /**
     * Verify the compilation schema is nullable after upgrade to 10.3
     * or later. (See DERBY-630)
     * @throws SQLException
     */
    public void testCompilationSchema() throws SQLException
    {        
       switch (getPhase())
        {
            case PH_CREATE:
            case PH_POST_SOFT_UPGRADE:
            case PH_POST_HARD_UPGRADE:
                // 10.0-10.2 inclusive had the system schema incorrect.
                if (!oldAtLeast(10, 3))
                    return;
                break;
        }

        DatabaseMetaData dmd = getConnection().getMetaData();

        ResultSet rs = dmd.getColumns(null, "SYS", "SYSSTATEMENTS", "COMPILATIONSCHEMAID");
        rs.next();
        assertEquals("SYS.SYSSTATEMENTS.COMPILATIONSCHEMAID IS_NULLABLE",
                        "YES", rs.getString("IS_NULLABLE"));
        rs.close();

        rs = dmd.getColumns(null, "SYS", "SYSVIEWS", "COMPILATIONSCHEMAID");
        rs.next();
        assertEquals("SYS.SYSVIEWS.COMPILATIONSCHEMAID IS_NULLABLE",
                        "YES", rs.getString("IS_NULLABLE"));
    }
    /**
     * In 10.3: We will write a LogRecord with a different format 
     * that can also write negative values.
     * 
     * Verify here that a 10.2 Database does not malfunction from this and
     * 10.2 Databases will work with the old LogRecord format.
     */
    public void testNegValueSupportedLogRecord()
        throws SQLException
    {
        switch(getPhase()) {
        case PH_CREATE: {
            // This case is derived from OnlineCompressTest.test6.
            Statement s = createStatement();
            s.execute("create table case606(keycol int, indcol1 int,"+
                "indcol2 int, data1 char(24), data2 char(24), data3 char(24)," +
                "data4 char(24), data5 char(24), data6 char(24),"+
                "data7 char(24), data8 char(24), data9 char(24)," + 
                "data10 char(24), inddec1 decimal(8), indcol3 int,"+
                "indcol4 int, data11 varchar(50))");
            s.close();
            break;
        }
        case PH_SOFT_UPGRADE:
            // Ensure that the old Log Record format is written
            // by Newer release without throwing any exceptions.
            checkDataToCase606(0, 2000);
            break;
        case PH_POST_SOFT_UPGRADE:
            // We are now back to Old release
            checkDataToCase606(0, 1000);
            break;

        case PH_HARD_UPGRADE:
            // Create the Derby606 bug scenario and test that
            // the error does not occur in Hard Upgrade
            checkDataToCase606(0, 94000);

            break;
        }
    }
    
    private void checkDataToCase606(int start_value, int end_value)
            throws SQLException {
        Statement s = createStatement();
        PreparedStatement insert_stmt = prepareStatement("insert into case606 values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        char[] data_dt = new char[24];
        char[] data_dt2 = new char[50];
        for (int i = 0; i < data_dt.length; i++)
            data_dt[i] = 'a';
        for (int i = 0; i < data_dt2.length; i++)
            data_dt2[i] = 'z';
        String data1_str = new String(data_dt);
        String data2_str = new String(data_dt2);

        for (int i = start_value; i < end_value; i++) {
            insert_stmt.setInt(1, i); // keycol
            insert_stmt.setInt(2, i * 10); // indcol1
            insert_stmt.setInt(3, i * 100); // indcol2
            insert_stmt.setString(4, data1_str); // data1_data
            insert_stmt.setString(5, data1_str); // data2_data
            insert_stmt.setString(6, data1_str); // data3_data
            insert_stmt.setString(7, data1_str); // data4_data
            insert_stmt.setString(8, data1_str); // data5_data
            insert_stmt.setString(9, data1_str); // data6_data
            insert_stmt.setString(10, data1_str); // data7_data
            insert_stmt.setString(11, data1_str); // data8_data
            insert_stmt.setString(12, data1_str); // data9_data
            insert_stmt.setString(13, data1_str); // data10_data
            insert_stmt.setInt(14, i * 20); // indcol3
            insert_stmt.setInt(15, i * 200); // indcol4
            insert_stmt.setInt(16, i * 50);
            insert_stmt.setString(17, data2_str); // data11_data

            insert_stmt.execute();
        }
        insert_stmt.close();
        commit();

        s.execute("delete from case606 where case606.keycol > 10000");
        commit();
        
        String schema = getTestConfiguration().getUserName();

        s.execute(
                "call SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE('"
                 + schema + "', 'CASE606',1,1,1)");
        s.close();
        commit();

    }
}
