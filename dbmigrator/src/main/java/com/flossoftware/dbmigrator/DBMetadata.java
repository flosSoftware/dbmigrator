package com.flossoftware.dbmigrator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;

public class DBMetadata {

	private String driver;
	private String connection;
	private String user;
	private String password;
	// private String table;
	private Connection connection2;

	public Connection getConnection2() {
		return connection2;
	}

	public void setConnection2(Connection connection2) {
		this.connection2 = connection2;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public DBMetadata(PropertiesConfiguration prop)
			throws ClassNotFoundException, SQLException {
		this.setDriver(prop.getString("driver"));
		this.setConnection(prop.getString("connection"));
		this.setUser(prop.getString("user"));
		this.setPassword(prop.getString("password"));
		// this.setTable(prop.getString("table"));
		try {
			Class.forName(getDriver());
		} catch (ClassNotFoundException e) {

			System.out.println("JDBC Driver " + getDriver() + " not found!!!");
			e.printStackTrace();
			return;

		}
		try {
			this.setConnection2(DriverManager.getConnection(getConnection(),
					getUser(), getPassword()));
		} catch (SQLException e) {

			System.out.println("Connection Failed!!!");
			e.printStackTrace();
			return;
		}
		
		//System.out.println(this);
	}
	
	public DBMetadata(HashMap<String, String> prop)
			throws ClassNotFoundException, SQLException {
		
//		for (String s : prop.values()) {
//
//			System.out.println(s);
//		}
		this.setDriver(prop.get("driver"));
		this.setConnection(prop.get("connection"));
		this.setUser(prop.get("user"));
		this.setPassword(prop.get("password"));
		// this.setTable(prop.getString("table"));
		try {
			Class.forName(getDriver());
		} catch (ClassNotFoundException e) {

			System.out.println("JDBC Driver " + getDriver() + " not found!!!");
			e.printStackTrace();
			return;

		}
		try {
			this.setConnection2(DriverManager.getConnection(getConnection(),
					getUser(), getPassword()));
		} catch (SQLException e) {

			System.out.println("Connection Failed!!!");
			e.printStackTrace();
			return;

		}
	}

	@Override
	public String toString() {
		return "DBMetadata [driver=" + driver + ", connection=" + connection + ", user=" + user + ", password="
				+ password + ", connection2=" + connection2 + "]";
	}

	public boolean hasDeletedColumn(String table) throws SQLException {
		List<String> list = getColumnsForTable(table);
		if (list != null && list.contains("deleted")) {
			return true;
		}
		return false;
	}

	public List<String> getColumnsForTable(String table) throws SQLException {

		ResultSet result = null;

		try {

			String catalog = null;
			String schemaPattern = "dbo";
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();
			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			List<String> cols = new ArrayList<>();

			while (result.next()) {
				String columnName = result.getString(4);
				cols.add(columnName);
			}

			return cols;

		} catch (Exception e) {
			System.out.println("!!!" + e);
		} finally {
			if (result != null)
				result.close();
		}

		return null;
	}

	private String getIsNullableSqlStmt(int i) {

		if (DatabaseMetaData.columnNoNulls == i) {
			return "NOT NULL";
		} else

			return "NULL";
	}

	public String getColumnDescription(String table, String colName)
			throws SQLException {

		String desc = "";
		ResultSet result = null;

		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(colName)) {
					desc += colName + " " + getSqlTypeName(result.getInt(5))
							+ "(" + result.getInt(7) + ") "
							+ getIsNullableSqlStmt(result.getInt(11))
							+ " DEFAULT " + result.getString(13);

					return desc;
				}
			}

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (result != null)
				result.close();
		}

		return desc;
	}

	public static String getSqlTypeName(int type) {
		switch (type) {
		case Types.BIT:
			return "BIT";
		case Types.TINYINT:
			return "TINYINT";
		case Types.SMALLINT:
			return "SMALLINT";
		case Types.INTEGER:
			return "INTEGER";
		case Types.BIGINT:
			return "BIGINT";
		case Types.FLOAT:
			return "FLOAT";
		case Types.REAL:
			return "REAL";
		case Types.DOUBLE:
			return "DOUBLE";
		case Types.NUMERIC:
			return "NUMERIC";
		case Types.DECIMAL:
			return "DECIMAL";
		case Types.CHAR:
			return "CHAR";
		case Types.VARCHAR:
			return "VARCHAR";
		case Types.LONGVARCHAR:
			return "LONGVARCHAR";
		case Types.DATE:
			return "DATE";
		case Types.TIME:
			return "TIME";
		case Types.TIMESTAMP:
			return "TIMESTAMP";
		case Types.BINARY:
			return "BINARY";
		case Types.VARBINARY:
			return "VARBINARY";
		case Types.LONGVARBINARY:
			return "LONGVARBINARY";
		case Types.NULL:
			return "NULL";
		case Types.OTHER:
			return "OTHER";
		case Types.JAVA_OBJECT:
			return "JAVA_OBJECT";
		case Types.DISTINCT:
			return "DISTINCT";
		case Types.STRUCT:
			return "STRUCT";
		case Types.ARRAY:
			return "ARRAY";
		case Types.BLOB:
			return "BLOB";
		case Types.CLOB:
			return "CLOB";
		case Types.REF:
			return "REF";
		case Types.DATALINK:
			return "DATALINK";
		case Types.BOOLEAN:
			return "BOOLEAN";
		case Types.ROWID:
			return "ROWID";
		case Types.NCHAR:
			return "NCHAR";
		case Types.NVARCHAR:
			return "NVARCHAR";
		case Types.LONGNVARCHAR:
			return "LONGNVARCHAR";
		case Types.NCLOB:
			return "NCLOB";
		case Types.SQLXML:
			return "SQLXML";
		}

		return "?";
	}

	public boolean isDateColumn(String table, String colName)
			throws SQLException {

		// System.out.println("Analyzing field " + colName);

		ResultSet result = null;
		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) == java.sql.Types.TIMESTAMP
								|| result.getInt(5) == java.sql.Types.DATE
								|| result.getString(6).equalsIgnoreCase("date") || result
								.getString(6).equalsIgnoreCase("datetime"))) {

					// System.out.println("TRUE");
					return true;
				} else if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) != java.sql.Types.TIMESTAMP
								&& result.getInt(5) != java.sql.Types.DATE
								&& !result.getString(6)
										.equalsIgnoreCase("date") && !result
								.getString(6).equalsIgnoreCase("datetime"))) {
					// System.out.println("FALSE: " + result.getString(6) + " "
					// + result.getInt(5) + " " + java.sql.Types.DATE);
					return false;
				}
			}

			return false;

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (result != null)
				result.close();
		}

		return false;
	}

	public boolean isNumberColumn(String table, String colName)
			throws SQLException {

		ResultSet result = null;
		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) == java.sql.Types.NUMERIC
								|| result.getInt(5) == java.sql.Types.DOUBLE
								|| result.getInt(5) == java.sql.Types.FLOAT
								|| result.getInt(5) == java.sql.Types.INTEGER
								|| result.getInt(5) == java.sql.Types.TINYINT
								|| result.getInt(5) == java.sql.Types.SMALLINT
								|| result.getInt(5) == java.sql.Types.BIGINT || result
								.getInt(5) == java.sql.Types.BIT)) {
					return true;
				} else if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) != java.sql.Types.NUMERIC
								&& result.getInt(5) != java.sql.Types.DOUBLE
								&& result.getInt(5) != java.sql.Types.FLOAT
								&& result.getInt(5) != java.sql.Types.INTEGER
								&& result.getInt(5) != java.sql.Types.TINYINT
								&& result.getInt(5) != java.sql.Types.SMALLINT
								&& result.getInt(5) != java.sql.Types.BIGINT && result
								.getInt(5) != java.sql.Types.BIT)) {
					// System.out.println("FALSE: "+ result.getString(6));
					return false;
				}
			}

			return false;

		} catch (Exception e) {
			System.out.println(e);
		} finally {

			if (result != null)
				result.close();
		}

		return false;
	}

	public boolean isCLOBColumn(String table, String colName)
			throws SQLException {

		ResultSet result = null;
		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) == java.sql.Types.CLOB || result
								.getInt(5) == java.sql.Types.NCLOB)) {
					return true;
				} else if (columnName.equalsIgnoreCase(colName)
						&& !(result.getInt(5) == java.sql.Types.CLOB || result
								.getInt(5) == java.sql.Types.NCLOB)) {
					// System.out.println("FALSE: "+ result.getString(6));
					return false;
				}
			}

			return false;

		} catch (Exception e) {
			System.out.println(e);
		} finally {

			if (result != null)
				result.close();
		}

		return false;
	}

	public boolean isBLOBColumn(String table, String colName)
			throws SQLException {

		ResultSet result = null;
		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(colName)
						&& (result.getInt(5) == java.sql.Types.BLOB)) {
					return true;
				} else if (columnName.equalsIgnoreCase(colName)
						&& !(result.getInt(5) == java.sql.Types.BLOB)) {
					// System.out.println("FALSE: "+ result.getString(6));
					return false;
				}
			}

			return false;

		} catch (Exception e) {
			System.out.println(e);
		} finally {

			if (result != null)
				result.close();
		}

		return false;
	}

	public List<String> getTables() throws SQLException {

		String catalog = 
				//getConnection2().getCatalog();
				null;
		//System.out.println("--- CATALOG --- "+getConnection2().getCatalog());

		String schemaPattern = isSQLServer() ? "dbo" : null;
		String tableNamePattern = null;
		String[] types = null;
		ResultSet result = null;

		List<String> tables = new ArrayList<>();

		try {
			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			if (this.isOracle()) {
				schemaPattern = databaseMetaData.getUserName();
			}

			// System.out.println("filtering tables with catalog: " + catalog +
			// " schema: " + schemaPattern);

			result = databaseMetaData.getTables(catalog, schemaPattern,
					tableNamePattern, types);

			while (result.next()) {
				String tableName = result.getString(3);
				tables.add(tableName);
			}
			return tables;
		} catch (Exception e) {
			// TODO: handle exception
			return tables;
		} finally {
			if (result != null)
				result.close();
		}
	}

	public int getTableSize(String table) throws SQLException {

		ResultSet resultSet = null;
		Statement statement = null;

		try {
			statement = getConnection2().createStatement();
			// System.out.println("executing statement: select count(*) from "
			// + table);

			String query = "select count(*) from " + table;
			if (hasDeletedColumn(table)) {
				query += " where deleted = 0";
			}

			resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}

			return -1;
		} catch (Exception e) {
			return -1;
		} finally {
			if (resultSet != null)
				resultSet.close();
			if (statement != null)
				statement.close();
		}

	}

	public boolean isNullableColumn(String table, String column)
			throws SQLException {

		ResultSet result = null;

		try {

			String catalog = null;
			String schemaPattern = null;
			String columnNamePattern = null;

			DatabaseMetaData databaseMetaData = getConnection2().getMetaData();

			result = databaseMetaData.getColumns(catalog, schemaPattern, table,
					columnNamePattern);

			while (result.next()) {
				String columnName = result.getString(4);
				if (columnName.equalsIgnoreCase(column)
						&& result.getInt(11) == DatabaseMetaData.columnNullable) {
					return true;
				} else if (columnName.equalsIgnoreCase(column)
						&& result.getInt(11) != DatabaseMetaData.columnNullable) {
					// System.out.println("FALSE: "+ result.getString(6));
					return false;
				}
			}

			return false;

		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (result != null)
				result.close();
		}

		return false;

	}

	public boolean isSQLServer() throws SQLException {
		DatabaseMetaData databaseMetaData = getConnection2().getMetaData();
		String productName = databaseMetaData.getDatabaseProductName();
		// System.out.println(productName);
		if (productName.toLowerCase().indexOf("sql server") != -1) {
			return true;
		} else
			return false;
	}

	public boolean isOracle() throws SQLException {
		DatabaseMetaData databaseMetaData = getConnection2().getMetaData();
		String productName = databaseMetaData.getDatabaseProductName();
		// System.out.println(productName);
		if (productName.toLowerCase().indexOf("oracle") != -1) {
			return true;
		} else
			return false;
	}

	public boolean isMySQL() throws SQLException {
		DatabaseMetaData databaseMetaData = getConnection2().getMetaData();
		String productName = databaseMetaData.getDatabaseProductName();
		// System.out.println(productName);
		if (productName.toLowerCase().indexOf("mysql") != -1) {
			return true;
		} else
			return false;
	}

}
