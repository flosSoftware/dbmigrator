package com.flossoftware.dbmigrator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

class CustomStringList<T> extends ArrayList<T> {

	private Map<String, String> map = null;
	private boolean foundInMap = false;

	public CustomStringList(Map<String, String> map) {
		this.map = map;
	}

	@Override
	public boolean contains(Object o) {
		String paramStr = (String) o;

		// System.out.println(paramStr);
		// if (this.map != null)
		// System.out.println(Arrays.toString(this.map.entrySet().toArray()));

		if (paramStr.length() > 0 && this.map != null
				&& this.map.containsKey(paramStr)) {

			this.setFoundInMap(true);

			// System.out.println("--- chiave " + paramStr + " trovata!");

			return true;
		}

		this.setFoundInMap(false);

		for (T s : this) {
			if (paramStr.equalsIgnoreCase((String) s))
				return true;
		}
		return false;
	}

	public String getCorresponding(Object o) {

		String paramStr = (String) o;

		if (paramStr.length() > 0 && this.map != null
				&& this.map.containsKey(paramStr)) {
			// System.out.println("--------- recupero il valore corrispondente: "
			// + this.map.get(paramStr));
			return this.map.get(paramStr);
		}

		for (T s : this) {
			if (paramStr.equalsIgnoreCase((String) s))
				return (String) s;
		}

		return "";
	}

	public void print() {
		for (T s : this) {
			System.out.println(s);
		}

	}

	public boolean isFoundInMap() {
		return foundInMap;
	}

	public void setFoundInMap(boolean foundInMap) {
		this.foundInMap = foundInMap;
	}
}

public class DBMigrator {

	public static void doTheJob() throws ConfigurationException,
			IOException, ClassNotFoundException, SQLException, WriteException {

		File f = new File("report");
		if (f.exists()) {
			FileUtils.deleteDirectory(f);
		}
		f.mkdir();

		File f2 = new File("script");
		if (f2.exists()) {
			FileUtils.deleteDirectory(f2);
		}
		f2.mkdir();

		File f3 = new File("script/import");
		if (f3.exists()) {
			FileUtils.deleteDirectory(f3);
		}
		f3.mkdirs();

		PropertiesConfiguration prop1 = new PropertiesConfiguration();
		prop1.load("config/from-db.properties");
		PropertiesConfiguration prop2 = new PropertiesConfiguration();
		prop2.load("config/to-db.properties");

		DBMetadata dbMeta = new DBMetadata(prop1);
		DBMetadata dbMeta2 = new DBMetadata(prop2);

		File tableMapF = new File("tableMap.properties");
		PropertiesConfiguration tableMapP = null;
		Map<String, String> tableMap = null;
		Map<String, String> invtableMap = null;
		if (tableMapF.exists()) {
			tableMapP = new PropertiesConfiguration();
			tableMapP.load(tableMapF);
			Iterator<String> keys = tableMapP.getKeys();
			tableMap = new HashMap<>();
			invtableMap = new HashMap<>();
			while (keys.hasNext()) {
				String next = keys.next();
				tableMap.put(next, tableMapP.getString(next));
				invtableMap.put(tableMapP.getString(next), next);
			}
		}

		CustomStringList<String> tables1 = new CustomStringList<String>(
				tableMap);
		tables1.addAll(dbMeta.getTables());
		CustomStringList<String> tables2 = new CustomStringList<String>(
				invtableMap);
		tables2.addAll(dbMeta2.getTables());

		WritableWorkbook workbook = Workbook.createWorkbook(new File(
				"report.xls"));

		String batchScript = "<?php\n\n"

		+ "// aggiungere a config.php\n" + "//		'ipFrom' => '"
				+ prop1.getString("ip")
				+ "',\n"
				+ "// 		'ipTo' => '"
				+ prop2.getString("ip")
				+ "',\n"
				+ "// 		'dbFrom' => '"
				+ prop1.getString("database")
				+ "',\n"
				+ "// 		'dbTo' => '"
				+ prop2.getString("database")
				+ "',\n"
				+ "// 		'usrFrom' => '"
				+ prop1.getString("user")
				+ "',\n"
				+ "// 		'usrTo' => '"
				+ prop2.getString("user")
				+ "',\n"
				+ "// 		'pwFrom' => '"
				+ prop1.getString("password")
				+ "',\n"
				+ "// 		'pwTo' => '"
				+ prop2.getString("password")
				+ "',\n\n"

				+ "set_time_limit(0);\n"
				+ "ini_set('default_charset', 'utf-8');\n"
				+ "if(!defined('sugarEntry'))define('sugarEntry', true);\n"
				+ "require_once ('include/entryPoint.php');\n\n"
				+

				"function executeInsertWithLob($db, $clobData, $blobData, $sql)\n"
				+ "{\n"
				+ "$db->checkConnection();\n"
				+ "if(empty($sql)){\n"
				+ "return false;\n"
				+ "}\n"
				+ "$lob_fields=array();\n"
				+ "$lob_field_type=array();\n"
				+ "$lobs=array();\n"
				+ "foreach ($clobData as $field => $value) {\n"
				+ "$lob_type = OCI_B_CLOB;\n"
				+ "$lob_fields[$field]=\":\".$field;\n"
				+ "$lob_field_type[$field]=$lob_type;\n"
				+ "}\n"
				+ "foreach ($blobData as $field => $value) {\n"
				+ "$lob_type = OCI_B_BLOB;\n"
				+ "$lob_fields[$field]=\":\".$field;\n"
				+ "$lob_field_type[$field]=$lob_type;\n"
				+ "}\n"
				+ "if (count($lob_fields) > 0 ) {\n"
				+ "$sql .= \" RETURNING \".implode(\",\", array_keys($lob_fields)).' INTO '.implode(\",\", array_values($lob_fields));\n"
				+ "}\n"
				+ "$GLOBALS['log']->info(\"Oracle Execute: $sql\");\n"
				+ "$stmt = oci_parse($db->database, $sql);\n"
				+ "if($db->checkError(\"Update parse failed: $sql\", false)) {\n"
				+ "return false;\n"
				+ "}\n"
				+ "foreach ($lob_fields as $key=>$descriptor) {\n"
				+ "$newlob = oci_new_descriptor($db->database, OCI_D_LOB);\n"
				+ "oci_bind_by_name($stmt, $descriptor, $newlob, -1, $lob_field_type[$key]);\n"
				+ "$lobs[$key] = $newlob;\n"
				+ "}\n"
				+ "$result = false;\n"
				+ "oci_execute($stmt,OCI_DEFAULT);\n"
				+ "if(!$db->checkError(\"Update execute failed: $sql\", false, $stmt) && oci_num_rows($stmt)) {\n"
				+ "foreach ($lobs as $key=>$lob){\n"
				+ "if (isset($clobData[$key])) {\n"
				+ "$val = from_html($clobData[$key]);\n"
				+ "} elseif (isset($blobData[$key])) {\n"
				+ "$val = from_html($blobData[$key]);\n"
				+ "} else {\n"
				+ "$val = null;\n"
				+ "}\n"
				+ "$lob->save($val);\n"
				+ "}\n"
				+ "oci_commit($db->database);\n"
				+ "$result = true;\n"
				+ "}\n"
				+ "foreach ($lobs as $lob){\n"
				+ "$lob->free();\n"
				+ "}\n"
				+ "oci_free_statement($stmt);\n"
				+ "return $result;\n"
				+ "}"
				+

				"function isNullOrEmptyString($string){\n"
				+ "return (!isset($string) || trim($string)==='');\n"
				+ "}\n"

				+ "function _convertToUTF8_2($content) {\n"
				+ "return iconv('', 'UTF8', $content);\n"
				+ "}\n"

				+ "function _convertToUTF8($content) {\n"
				+ "if(!mb_check_encoding($content, 'UTF-8')) {\n"
				+ "$content = mb_convert_encoding($content, 'UTF-8', 'auto');\n"
				+ "}\n"
				+ "return $content;\n"
				+ "}\n"

				+ "function process_items(&$item, $key)\n"
				+ "{\n"
				+ "$item = _convertToUTF8_2($item);\n"
				+ "//$item = _convertToUTF8($item);\n"
				+ "$item = trim($item);\n"
				+ "}\n"

				+ "function ms_escape_string($data) {\n"
				+ "if ( isNullOrEmptyString($data) ) return $data;\n"
				+ "if ( is_numeric($data) ) return $data;\n"
				+ "$non_displayables = array(\n"
				+ "'/%0[0-8bcef]/',            // url encoded 00-08, 11, 12, 14, 15\n"
				+ "'/%1[0-9a-f]/',             // url encoded 16-31\n"
				+ "'/[\\x00-\\x08]/',            // 00-08\n"
				+ "'/\\x0b/',                   // 11\n"
				+ "'/\\x0c/',                   // 12\n"
				+ "'/[\\x0e-\\x1f]/'             // 14-31\n" + ");\n"
				+ "foreach ( $non_displayables as $regex )\n"
				+ "$data = preg_replace( $regex, '', $data );\n"
				+ "$data = str_replace(\"'\", \"''\", $data );\n"
				+ "return $data;\n" + "}\n"

				+ "function array_ms_escape_string($array) {\n"
				+ "$ret = array();\n" + "foreach($array as $k => $v) {\n"
				+ "$ret[$k] = ms_escape_string($v);\n" + "}\n"
				+ "return $ret;\n" + "}\n"

				+ "function array_my_escape_string($array, $conn) {\n"
				+ "$ret = array();\n" + "foreach($array as $k => $v) {\n"
				+ "$ret[$k] = mysqli_real_escape_string($conn, $v);\n" + "}\n"
				+ "return $ret;\n" + "}\n"

				+ "function isTestImport()\n" + "{\n" + "return false;\n"
				+ "}\n\n"

				+ "global $sugar_config;\nglobal $db;\n";

		// analyzeTables(workbook, "mce_comuni", "mm_comuni", prop1,
		// prop2);

		for (String string : tables2) {

			File mapPropFile = new File("map/" + string + ".properties");

			PropertiesConfiguration mapProp = null;
			Map<String, String> map = null;
			Map<String, String> invMap = null;

			if (mapPropFile.exists()) {

				mapProp = new PropertiesConfiguration();
				mapProp.load("map/" + string + ".properties");

				Iterator<String> keys = mapProp.getKeys();
				map = new HashMap<>();
				invMap = new HashMap<>();
				while (keys.hasNext()) {
					String next = keys.next();
					map.put(next, mapProp.getString(next));
					invMap.put(mapProp.getString(next), next);
				}

			}

//			if (string.equals("documents")) {

//				System.out.println("documents");

				// System.out.println(Arrays.toString(map.entrySet().toArray()));
				// System.out
				// .println(Arrays.toString(invMap.entrySet().toArray()));

				if (dbMeta2.getTableSize(string) > 0
						&& tables1.contains(string)) {

					System.out.println("table " + string
							+ " will be analyzed...");
					analyzeTable(workbook, tables1.getCorresponding(string),
							string, prop1, prop2, map, invMap);

					batchScript += "require('" + "import/import_"
							+ tables1.getCorresponding(string) + ".php');\n";
				} else if (dbMeta2.getTableSize(string) == 0
						&& tables1.contains(string)) {
					System.out.println("table " + string + " is empty");
					analyzeTable(workbook, tables1.getCorresponding(string),
							string, prop1, prop2, map, invMap);

					batchScript += "require('" + "import/import_"
							+ tables1.getCorresponding(string) + ".php');\n";
				} else {
					System.out.println("table " + string
							+ " not present on the other db");
				}
//			}
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(
				"script/import.php"));

		bw.write(batchScript);

		bw.close();

		workbook.write();
		workbook.close();
	}

	private static String serverConn(boolean isTo, String serverType) {

		String ret = "";

		if (serverType.equals("sqlsrv")) {

			ret = (isTo ? "$to" : "$from") + "SqlSrvConn";

		} else if (serverType.equals("mysql")) {

			ret = (isTo ? "$to" : "$from") + "MySQLConn";

		} else {

			;

		}

		return ret;

	}

	private static String arrayQuoteStmt(String serverType, String row,
			String conn) {

		String ret = "";

		if (serverType.equals("sqlsrv")) {

			ret = row + " = array_ms_escape_string(" + row + ");\n\n";

		} else if (serverType.equals("mysql")) {

			ret = row + " = array_my_escape_string(" + row + "," + conn
					+ ");\n\n";

		} else {

			ret = row + " = $db->arrayQuote(" + row + ");\n\n";

		}

		return ret;
	}

	private static String queryStmt(String serverType, String conn,
			String query, String result, boolean blobInsert) {

		String ret = "";

		if (serverType.equals("sqlsrv")) {

			ret = result
					+ " = sqlsrv_query("
					+ conn
					+ ","
					+ query
					+ ");\n\n"
					+ "if("
					+ result
					+ " === false) {\n"
					+ "$GLOBALS['log']->fatal(print_r( sqlsrv_errors(), true));\n"
					+ "}\n\n";

		} else if (serverType.equals("mysql")) {

			ret = result + " = mysqli_query(" + conn + "," + query + ");\n\n"
					+ "if(" + result + " === false) {\n"
					+ "$GLOBALS['log']->fatal(mysqli_error(" + conn + "));\n"
					+ "}\n\n";

		} else if (blobInsert) {

			ret = "executeInsertWithLob($db, $clobData, $blobData, " + query
					+ ");\n\n";

		} else {

			ret = result + " = $db->query(" + query + ");\n\n";

		}

		return ret;
	}

	private static void renderToGenericTemplate(String fromServer,
			String toServer, String table1, String table2,
			PropertiesConfiguration prop1, PropertiesConfiguration prop2,
			String deleteStmts, String insertStmts, DBMetadata dbMeta1,
			DBMetadata dbMeta2) throws IOException, SQLException {

		String whereDel = dbMeta1.hasDeletedColumn(table1) ? " where deleted = 0"
				: "";

		String preambleStmt = "", connStmt = "", fetchStmt = "", closeStmt = "", quoteStmt = "";

		// from server

		if (fromServer.equals("sqlsrv")) {

			connStmt = "$fromSqlSrvOptions = array(\n"
					+ "\"Database\"=>$sugar_config['dbFrom'],\n"
					+ "\"UID\" => $sugar_config['usrFrom'],\n"
					+ "\"PWD\" => $sugar_config['pwFrom'],\n"
					+ "\"CharacterSet\" => \"UTF-8\",\n"
					+ "\"ReturnDatesAsStrings\" => true,\n"
					+ "\"MultipleActiveResultSets\" => true,\n"
					+ ");"
					+ serverConn(false, fromServer)
					+ " = sqlsrv_connect($sugar_config['ipFrom'], $fromSqlSrvOptions)\n"
					+ "or die(print_r(sqlsrv_errors(), true));\n\n";

			fetchStmt = "$query = \"select \" . (isTestImport() ? \"TOP 10\" : \"*\") . \" from {$sugar_config['dbFrom']}."
					+ table1
					+ whereDel
					+ " \";\n"
					+ queryStmt("sqlsrv", serverConn(false, fromServer),
							"$query", "$result", false)
					+ "while ($row = sqlsrv_fetch_array($result, SQLSRV_FETCH_ASSOC)) {\n\n";

			closeStmt = "sqlsrv_close(" + serverConn(false, fromServer)
					+ ");\n\n";

		} else if (fromServer.equals("mysql")) {

			connStmt = serverConn(false, fromServer)
					+ " = mysqli_connect($sugar_config['ipFrom'],$sugar_config['usrFrom'],$sugar_config['pwFrom'],$sugar_config['dbFrom']);\n\n"
					+

					"if (mysqli_connect_errno()) { die(print_r(mysqli_connect_error(), true)); }\n\n";

			fetchStmt = "$query = \"select * from {$sugar_config['dbFrom']}."
					+ table1
					+ whereDel
					+ " \" . (isTestImport() ? \"LIMIT 0, 10\" : \"\").\"\";\n\n"
					+

					queryStmt("mysql", serverConn(false, fromServer), "$query",
							"$result", false) +

					"while ($row = mysqli_fetch_array($result)) {\n\n";

			closeStmt = "mysqli_close(" + serverConn(false, fromServer)
					+ ");\n\n";

		} else {

			connStmt = "\n\n";

			fetchStmt = "$query = \"select * from {$sugar_config['dbFrom']}."
					+ table1
					+ whereDel
					+ " \" . (isTestImport() ? \"LIMIT 0, 10\" : \"\").\"\";\n\n"
					+

					queryStmt("", "", "$query", "$result", false)

					+

					"while ($row = $db->fetchByAssoc($result, -1, false)) {\n\n";

			closeStmt = "\n\n";
		}

		// to server

		if (toServer.equals("sqlsrv")) {

			connStmt += "$toSqlSrvOptions = array(\n"
					+ "\"Database\"=>$sugar_config['dbTo'],\n"
					+ "\"UID\" => $sugar_config['usrTo'],\n"
					+ "\"PWD\" => $sugar_config['pwTo'],\n"
					+ "\"CharacterSet\" => \"UTF-8\",\n"
					+ "\"ReturnDatesAsStrings\" => true,\n"
					+ "\"MultipleActiveResultSets\" => true,\n"
					+ ");"
					+ serverConn(true, toServer)
					+ " = sqlsrv_connect($sugar_config['ipTo'], $toSqlSrvOptions)\n"
					+ "or die(print_r(sqlsrv_errors(), true));\n\n";

			preambleStmt += queryStmt("sqlsrv", serverConn(true, toServer),
					"\"SET DATEFORMAT ymd\"", "$result", false);

			quoteStmt = arrayQuoteStmt(toServer, "$row",
					serverConn(true, toServer));

			closeStmt += "sqlsrv_close(" + serverConn(true, toServer)
					+ ");\n\n";

		} else if (toServer.equals("mysql")) {

			connStmt += serverConn(true, toServer)
					+ " = mysqli_connect($sugar_config['ipTo'],$sugar_config['usrTo'],$sugar_config['pwTo'],$sugar_config['dbTo']);\n\n"
					+

					"if (mysqli_connect_errno()) { die(print_r(mysqli_connect_error(), true)); }\n\n";

			quoteStmt = arrayQuoteStmt(toServer, "$row",
					serverConn(true, toServer));

			closeStmt += "mysqli_close(" + serverConn(true, toServer)
					+ ");\n\n";

		} else {

			quoteStmt = arrayQuoteStmt("", "$row", "");
		}

		String result = "<?php\n"
				+ "if(!defined('sugarEntry'))define('sugarEntry', true);\n"
				+ "require_once 'include/entryPoint.php';\n\n"
				+ (fromServer.equals("sugar") || toServer.equals("sugar") ? "global $db;\n"
						: "")

				+ "$GLOBALS['log']->fatal('Start import " + table1 + "');\n\n"

				+ connStmt + "\n\n"

				+ preambleStmt

				+ deleteStmts + "\n\n"

				+ fetchStmt +

				quoteStmt +

				"array_walk_recursive($row, 'process_items');\n\n" +

				insertStmts + "\n\n" +

				"}\n\n" +

				closeStmt +

				"$GLOBALS['log']->fatal('End import " + table1 + "');";

		BufferedWriter bw = new BufferedWriter(new FileWriter("script/import/"
				+ "import_" + table1 + ".php"));

		bw.write(result);

		bw.close();
	}

	private static void generateXLS(String table1, String table2,
			String header1, String header2, WritableWorkbook workbook,
			List<String> missingFields1, List<String> missingFields2,
			DBMetadata dbMeta1, DBMetadata dbMeta2)
			throws RowsExceededException, WriteException, IOException {

		WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 12,
				WritableFont.BOLD, true);
		WritableCellFormat headerFormat = new WritableCellFormat(headerFont);

		WritableSheet sheet = workbook.createSheet(table1 + " - " + table2,
				workbook.getNumberOfSheets());

		int numOfRecord = 0;
		int numOfRecord2 = 0;

		try {
			numOfRecord = dbMeta1.getTableSize(table1);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			numOfRecord2 = dbMeta2.getTableSize(table2);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		sheet.addCell(new Label(0, 0, "Number of rows in " + header1 + "." + table1,
				headerFormat));
		sheet.addCell(new Number(0, 1, numOfRecord));
		sheet.addCell(new Label(1, 0, "Number of rows in " + header2 + "." + table2,
				headerFormat));
		sheet.addCell(new Number(1, 1, numOfRecord2));

		int col = 2;
		int row = 0;

		sheet.addCell(new Label(col, row, "Fields missing in " + header1 + "." + table1,
				headerFormat));
		row++;

		for (String string : missingFields1) {
			String goodField = string;
			sheet.addCell(new Label(col, row, goodField));
			row++;
		}

		col = 3;
		row = 0;

		sheet.addCell(new Label(col, row, "Fields missing in " + header2 + "." + table2,
				headerFormat));
		row++;

		String alter = "ALTER TABLE " + table2 + " ADD (";

		for (String string : missingFields2) {

			String goodField = string;
			try {
				alter += " " + dbMeta1.getColumnDescription(table1, goodField)
						+ ",\n";
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			sheet.addCell(new Label(col, row, string));
			row++;
		}

		alter += ");";

		sheet.addCell(new Label(0, row, "ALTER Statement", headerFormat));

		row++;

		sheet.addCell(new Label(0, row, alter));

	}

	private static void analyzeTable(WritableWorkbook workbook, String table1,
			String table2, PropertiesConfiguration prop1,
			PropertiesConfiguration prop2, Map<String, String> map,
			Map<String, String> invMap) throws IOException,
			ClassNotFoundException, SQLException, RowsExceededException,
			WriteException {

		System.out.println("analyzing table " + table1 + " - " + table2);

		BufferedWriter bw1 = new BufferedWriter(new FileWriter("report/"
				+ prop1.getString("database") + "-" + table1 + ".txt"));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("report/"
				+ prop2.getString("database") + "-" + table2 + ".txt"));

		CustomStringList<String> arr1 = new CustomStringList<String>(map);
		CustomStringList<String> arr2 = new CustomStringList<String>(invMap);

		DBMetadata dbMeta1 = new DBMetadata(prop1);

		for (String line : dbMeta1.getColumnsForTable(table1)) {

			String str = line + "\n";
			arr1.add(line);
			bw1.write(str);
		}

		DBMetadata dbMeta2 = new DBMetadata(prop2);

		boolean addDBName = true;

		boolean isSqlServer1 = dbMeta1.isSQLServer();
		boolean isSqlServer2 = dbMeta2.isSQLServer();
		boolean isMySQL1 = dbMeta1.isMySQL();
		boolean isMySQL2 = dbMeta2.isMySQL();
		boolean isOracle1 = dbMeta1.isOracle();
		boolean isOracle2 = dbMeta2.isOracle();

		String fromServerType = "", toServerType = "";
		String toServerConn = "", fromServerConn = "";

		if (isSqlServer1) {
			fromServerType = "sqlsrv";
		} else if (isMySQL1) {
			fromServerType = "mysql";
		} else if (isOracle1) {
			fromServerType = "oracle";
		}

		if (prop1.getString("server_type_override") != null
				&& !prop1.getString("server_type_override").isEmpty()) {
			fromServerType = prop1.getString("server_type_override");
		}

		if (isSqlServer2) {
			toServerType = "sqlsrv";
		} else if (isMySQL2) {
			toServerType = "mysql";
		} else if (isOracle2) {
			toServerType = "oracle";
		}

		if (prop2.getString("server_type_override") != null
				&& !prop2.getString("server_type_override").isEmpty()) {
			toServerType = prop2.getString("server_type_override");
		}

		boolean addTeam = prop2.getBoolean("add_team");

		toServerConn = serverConn(true, toServerType);
		fromServerConn = serverConn(false, fromServerType);

		String delimiterOp = "`";
		String delimiterCl = "`";

		if (isSqlServer2) {

			delimiterOp = "[";
			delimiterCl = "]";

		}

		if (isOracle2) {

			addDBName = false;

			delimiterOp = "\\\"";
			delimiterCl = "\\\"";

		}

		String now = "";
		if (isOracle2) {
			now = "SYSDATE";
		} else if (isSqlServer2) {
			now = "GETDATE()";
		} else {
			now = "NOW()";
		}

		for (String line : dbMeta2.getColumnsForTable(table2)) {

			String str = line + "\n";
			arr2.add(line);
			bw2.write(str);
		}

		Collections.sort(arr1);
		Collections.sort(arr2);

		CustomStringList<String> notInArr1 = new CustomStringList<String>(map);
		CustomStringList<String> notInArr2 = new CustomStringList<String>(
				invMap);

		List<String> insertArr2 = new ArrayList<>();
		List<String> dateColArr1 = new ArrayList<>();
		List<String> nullColArr1 = new ArrayList<>();
		List<String> notNullColArr1 = new ArrayList<>();
		List<String> notNullNumberColArr1 = new ArrayList<>();
		List<String> nullNumberColArr1 = new ArrayList<>();
		List<String> nullCLOBColArr1 = new ArrayList<>();
		List<String> nullBLOBColArr1 = new ArrayList<>();
		List<String> notNullCLOBColArr1 = new ArrayList<>();
		List<String> notNullBLOBColArr1 = new ArrayList<>();

		String fields = "", values = "", nullableCmd = "", dateCmd = "", notNullableNumCmd = "", nullableNumCmd = "", clobData = "", blobData = "";

		for (String string : arr1) {

			if (!arr2.contains(string)) {
				notInArr2.add(string);
			} else {
				if (!insertArr2.contains(arr2.getCorresponding(string)))
					insertArr2.add(arr2.getCorresponding(string));
			}
		}

		// System.out.println(Arrays.toString(insertArr2.toArray()));

		if (notInArr2.size() >= 1) {
			bw1.write("\n");
			bw1.write("[missing fields] \n");
		}

		for (String string : notInArr2)
			bw1.write(string + "\n");

		for (String string : arr2) {
			if (!arr1.contains(string)) {
				notInArr1.add(string);
			}
		}

		if (notInArr1.size() >= 1) {
			bw2.write("\n");
			bw2.write("[missing fields] \n");
		}

		for (String string : notInArr1)
			bw2.write(string + "\n");

		generateXLS(table1, table2, prop1.getString("database"),
				prop2.getString("database"), workbook, notInArr1, notInArr2,
				dbMeta1, dbMeta2);

		if (insertArr2.size() > 0) {

			bw2.write("\n");
			bw2.write("[fields to insert] \n");

			for (String string : insertArr2) {

				bw2.write(delimiterOp + string + delimiterCl + ", ");
				fields += delimiterOp + string + delimiterCl + ", ";
			}

			fields = fields.substring(0, fields.length() - 2);

			bw2.write("\n");

			bw2.write("[values to insert] \n");

			for (String string : insertArr2) {

				String colName1Long = arr1.getCorresponding(string);
				String colName1 = colName1Long;
				String colName2 = string;

				String val = null;

				// id
				if (colName2.equalsIgnoreCase("id")
						&& prop2.getBoolean("newId")) {

					String id = "";

					if (toServerType.equals("sugar")) {

						id = "'{$id}'";

					} else if (toServerType.equals("sqlsrv")) {

						id = "NEWID()";

					} else if (toServerType.equals("mysql")) {

						id = "UUID()";

					} else if (toServerType.equals("oracle")) {

						id = "sys_guid()";

					} else {

						;

					}
					val = id + ", ";
				}
				// le date e i numeri li tratto a parte
				else if ((isSqlServer1 || isSqlServer2 || isOracle2)
						&& dbMeta2.isDateColumn(table2, colName2)) {

					if (dbMeta2.isNullableColumn(table2, colName2)) {
						nullColArr1.add(colName1);
					}

					dateColArr1.add(colName1);
					val = "{$" + colName1 + "}, ";

				}

				else if (dbMeta2.isNumberColumn(table2, colName2)

				&& dbMeta2.isNullableColumn(table2, colName2)) {

					nullNumberColArr1.add(colName1);
					val = "{$" + colName1 + "}, ";

				} else if (dbMeta2.isNumberColumn(table2, colName2)

				&& !dbMeta2.isNullableColumn(table2, colName2)) {

					notNullNumberColArr1.add(colName1);
					val = "{$" + colName1 + "}, ";

				} else if (isOracle2 && dbMeta2.isCLOBColumn(table2, colName2)
						&& dbMeta2.isNullableColumn(table2, colName2)) {

					nullCLOBColArr1.add(colName1);
					val = "EMPTY_CLOB(), ";

				} else if (isOracle2 && dbMeta2.isBLOBColumn(table2, colName2)
						&& dbMeta2.isNullableColumn(table2, colName2)) {

					nullBLOBColArr1.add(colName1);
					val = "EMPTY_BLOB(), ";

				} else if (isOracle2 && dbMeta2.isCLOBColumn(table2, colName2)
						&& !dbMeta2.isNullableColumn(table2, colName2)) {

					notNullCLOBColArr1.add(colName1);
					val = "EMPTY_CLOB(), ";

				} else if (isOracle2 && dbMeta2.isBLOBColumn(table2, colName2)
						&& !dbMeta2.isNullableColumn(table2, colName2)) {

					notNullBLOBColArr1.add(colName1);
					val = "EMPTY_BLOB(), ";

				} else if (dbMeta2.isNullableColumn(table2, colName2)) {

					nullColArr1.add(colName1);
					val = "{$" + colName1 + "}, ";

				} else if (!dbMeta2.isNullableColumn(table2, colName2)) {

					notNullColArr1.add(colName1);
					val = "{$" + colName1 + "}, ";

				} else {

					val = "'{$row['" + colName1 + "']}', ";

				}

				values += val;

				bw2.write(val);
			}

			values = values.substring(0, values.length() - 2);

			bw2.write("\n");
		}

		if (notNullNumberColArr1.size() > 0) {

			bw2.write("[commands for not nullable number fields] \n");

			for (String string : notNullNumberColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"0\" : \"{$row['" + string
						+ "']}\";";

				bw2.write(cmd + "\n");

				notNullableNumCmd += cmd + "\n";
			}

		}

		if (nullNumberColArr1.size() > 0) {

			bw2.write("[commands for nullable number fields] \n");

			for (String string : nullNumberColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"NULL\" : \"{$row['" + string
						+ "']}\";";

				bw2.write(cmd + "\n");

				nullableNumCmd += cmd + "\n";
			}

		}

		if (nullColArr1.size() > 0) {

			bw2.write("[commands for nullable fields] \n");

			for (String string : nullColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"NULL\" : \"'{$row['" + string
						+ "']}'\";";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";
			}

		}

		if (notNullColArr1.size() > 0) {

			bw2.write("[commands for not nullable fields] \n");

			for (String string : notNullColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"''\" : \"'{$row['" + string
						+ "']}'\";";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";
			}

		}

		if (nullCLOBColArr1.size() > 0) {

			bw2.write("[commands for nullable CLOB fields] \n");

			for (String string : nullCLOBColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? null : $row['" + string + "'];";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";

				String corr = arr2.getCorresponding(string);

				clobData += "\"" + corr + "\"" + " => $" + string + ", \n";
			}

		}

		if (nullBLOBColArr1.size() > 0) {

			bw2.write("[commands for nullable BLOB fields] \n");

			for (String string : nullBLOBColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? null : $row['" + string + "'];";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";

				String corr = arr2.getCorresponding(string);

				blobData += "\"" + corr + "\"" + " => $" + string + ", \n";
			}

		}

		if (notNullCLOBColArr1.size() > 0) {

			bw2.write("[commands for not nullable CLOB fields] \n");

			for (String string : notNullCLOBColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"\" : $row['" + string + "'];";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";

				String corr = arr2.getCorresponding(string);

				clobData += "\"" + corr + "\"" + " => $" + string + ", \n";
			}

		}

		if (notNullBLOBColArr1.size() > 0) {

			bw2.write("[commands for not nullable BLOB fields] \n");

			for (String string : notNullBLOBColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"\" : $row['" + string + "'];";

				bw2.write(cmd + "\n");

				nullableCmd += cmd + "\n";

				String corr = arr2.getCorresponding(string);

				blobData += "\"" + corr + "\"" + " => $" + string + ", \n";
			}

		}

		if (dateColArr1.size() > 0) {

			bw2.write("[commands to convert SQL Server date fields] \n");

			for (String string : dateColArr1) {

				if ((isSqlServer1 || isSqlServer2)) {

					String cmd = "if(!isNullOrEmptyString($row['" + string
							+ "'])) { \n"
							+ "$date = date(\"Y-m-d H:i:s\", strtotime($row['"
							+ string + "'])); \n" + "$" + string
							+ " = \"'{$date}'\"; \n" + "}";

					bw2.write(cmd + "\n");

					dateCmd += cmd + "\n";

				} else if (isOracle2) {

					String cmd = "if(!isNullOrEmptyString($row['"
							+ string
							+ "'])) { \n"
							+ "$date = date(\"Y-m-d H:i:s\", strtotime($row['"
							+ string
							+ "'])); \n"
							+ "$"
							+ string
							+ " = \"to_date('{$date}', 'YYYY-MM-DD HH24:MI:SS')\"; \n"
							+ "}";

					bw2.write(cmd + "\n");

					dateCmd += cmd + "\n";

				}

			}

		}

		String delTableId = addDBName ? "{$sugar_config['dbTo']}." + table2
				: "" + table2;

		if (isSqlServer2) {
			delTableId = addDBName ? (delimiterOp + "{$sugar_config['dbTo']}"
					+ delimiterCl + ".[dbo].")
					+ table2 : "" + delimiterOp + table2 + delimiterCl;
		}

		String delScript = "$query = \"delete from "
				+ delTableId
				+ "\";\n"
				+ queryStmt(toServerType, toServerConn, "$query", "$result",
						false);

		String tableId = addDBName ? "{$sugar_config['dbTo']}." + table2 : ""
				+ table2;

		if (isSqlServer2) {
			tableId = addDBName ? (delimiterOp + "{$sugar_config['dbTo']}"
					+ delimiterCl + ".[dbo].")
					+ table2 : "" + delimiterOp + table2 + delimiterCl;
		}

		if (prop2.getBoolean("newId")
				&& fields.indexOf(delimiterOp + "id" + delimiterCl) == -1) {

			fields = delimiterOp + "id" + delimiterCl + ", " + fields;

			String id = "";

			if (toServerType.equals("sugar")) {

				id = "'{$id}'";

			} else if (toServerType.equals("sqlsrv")) {

				id = "NEWID()";

			} else if (toServerType.equals("mysql")) {

				id = "UUID()";

			} else if (toServerType.equals("oracle")) {

				id = "sys_guid()";

			} else {

				;

			}
			values = id + ", " + values;
		}

		String script =

		notNullableNumCmd
				+ "\n"
				+ nullableNumCmd
				+ "\n"
				+ nullableCmd
				+ "\n"
				+ dateCmd
				+ "\n"
				+ (toServerType.equals("sugar") ? "$id = create_guid();\n" : "")

				+ "$q=\"insert into " + tableId + "  \" . \n" + "\"( \" . \n" +

				"\"" + fields + "\" . \n" +

				"\") \" . \n" +

				"\"values \" . \n" + "\"(\" . \n" +

				"\"" + values + "\" . \n" +

				"\")\";\n";

		if (!clobData.isEmpty() || !blobData.isEmpty()) {
			script += "$clobData = array(\n" + clobData
					+ ");\n$blobData = array(\n" + blobData + ");\n\n"
					+ "executeInsertWithLob($db, $clobData, $blobData, $q);";
		} else {
			script += queryStmt(toServerType, toServerConn, "$q", "$res", false);
		}

		if (notInArr2.contains("team_id\n") && addTeam) {

			StringTokenizer tok = new StringTokenizer(table2, "_");
			String module = "";

			while (tok.hasMoreTokens()) {
				char[] stringArray = tok.nextToken().toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				module += new String(stringArray);
			}

			String teamTableIdTo = addDBName ? "{$sugar_config['dbTo']}.securitygroups_records"
					: "" + "securitygroups_records";

			if (isSqlServer2) {
				teamTableIdTo = addDBName ? (delimiterOp
						+ "{$sugar_config['dbTo']}" + delimiterCl + ".[dbo]." + "securitygroups_records")
						: "" + delimiterOp + "securitygroups_records"
								+ delimiterCl;
			}

			String scriptTeam =

			"\n$id = create_guid(); \n" +

			"$q=\"insert into "

					+ teamTableIdTo

					+ " \" . \n"
					+ "\"( \" . \n"
					+

					"\""
					+ delimiterOp
					+ "id"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "securitygroup_id"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "record_id"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "module"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "date_modified"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "modified_user_id"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "created_by"
					+ delimiterCl
					+ ", "
					+ delimiterOp
					+ "deleted"
					+ delimiterCl
					+ "\" . \n"
					+

					"\") \" . \n"
					+

					"\"values \" . \n"
					+ "\"(\" . \n"
					+

					"\"'{$id}', '{$row['team_id']}', '{$row['id']}', '"
					+ module
					+ "', "
					+ now
					+ ", '{$row['modified_user_id']}', '{$row['created_by']}', '{$row['deleted']}'\" . \n"
					+

					"\")\";\n" +

					queryStmt(toServerType, toServerConn, "$q", "$res", false);

			script += "\n" + scriptTeam;

			String teamDelScript = "$query = \"delete from "
					+ teamTableIdTo
					+ " where module = '"

					+ module
					+ "'\";\n"
					+

					queryStmt(toServerType, toServerConn, "$query", "$result",
							false);

			delScript += "\n" + teamDelScript;
		}

		bw2.write("[script] \n");
		bw2.write(script);

		renderToGenericTemplate(fromServerType, toServerType, table1, table2,
				prop1, prop2, delScript, script, dbMeta1, dbMeta2);

		bw1.close();
		bw2.close();
	}

	private static void analyzeTables(WritableWorkbook workbook, String table1,
			String table2, PropertiesConfiguration prop1,
			PropertiesConfiguration prop2, Map<String, String> map,
			Map<String, String> invMap) throws IOException,
			ClassNotFoundException, SQLException, RowsExceededException,
			WriteException {

		BufferedWriter bw1 = new BufferedWriter(new FileWriter("report/"
				+ prop1.getString("database") + "-" + table1 + ".txt"));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("report/"
				+ prop2.getString("database") + "-" + table2 + ".txt"));

		List<String> arr1 = new ArrayList<>();
		List<String> arr2 = new ArrayList<>();

		DBMetadata dbMeta1 = new DBMetadata(prop1);
		DBMetadata dbMeta2 = new DBMetadata(prop2);

		for (String line : dbMeta1.getColumnsForTable(table1)) {

			String str = line + "\n";
			arr1.add(line);
			bw1.write(str);
		}

		boolean isSqlServer1 = dbMeta1.isSQLServer();
		boolean isSqlServer2 = dbMeta2.isSQLServer();
		boolean isMySQL1 = dbMeta1.isMySQL();
		boolean isMySQL2 = dbMeta2.isMySQL();
		boolean isOracle1 = dbMeta1.isOracle();
		boolean isOracle2 = dbMeta2.isOracle();

		String fromServerType = "", toServerType = "";
		String toServerConn = "", fromServerConn = "";

		if (isSqlServer1) {
			fromServerType = "sqlsrv";
		} else if (isMySQL1) {
			fromServerType = "mysql";
		} else if (isOracle1) {
			fromServerType = "oracle";
		}

		if (isSqlServer2) {
			toServerType = "sqlsrv";
		} else if (isMySQL2) {
			toServerType = "mysql";
		} else if (isOracle2) {
			toServerType = "oracle";
		}

		toServerConn = serverConn(true, toServerType);
		fromServerConn = serverConn(false, fromServerType);

		for (String line : dbMeta2.getColumnsForTable(table2)) {

			String str = line + "\n";
			arr2.add(line);
			bw2.write(str);
		}

		Collections.sort(arr1);
		Collections.sort(arr2);

		List<String> notInArr1 = new ArrayList<>();
		List<String> notInArr2 = new ArrayList<>();

		List<String> insertArr1 = new ArrayList<>();
		List<String> dateColArr1 = new ArrayList<>();
		List<String> nullColArr1 = new ArrayList<>();
		List<String> notNullNumberColArr1 = new ArrayList<>();

		String fields = "", values = "", nullableCmd = "", dateCmd = "", notNullableNumCmd = "";

		for (String string : arr1) {

			if (!arr2.contains(string)) {
				notInArr2.add(string);
			} else {
				insertArr1.add(string);
			}
		}

		if (notInArr2.size() >= 1) {
			bw1.write("\n");
			bw1.write("[missing fields] \n");
		}

		for (String string : notInArr2)
			bw1.write(string);

		for (String string : arr2) {
			if (!arr1.contains(string)) {
				notInArr1.add(string);
			}
		}

		if (notInArr1.size() >= 1) {
			bw2.write("\n");
			bw2.write("[missing fields] \n");
		}

		for (String string : notInArr1)
			bw2.write(string);

		generateXLS(table2, table2, prop1.getString("database"),
				prop2.getString("database"), workbook, notInArr1, notInArr2,
				dbMeta1, dbMeta2);

		if (insertArr1.size() > 0) {

			bw1.write("\n");
			bw1.write("[fields to insert] \n");

			for (String string : insertArr1) {

				bw1.write("`" + string + "`, ");
				fields += "`" + string + "`, ";
			}

			fields = fields.substring(0, fields.length() - 2);

			bw1.write("\n");

			bw1.write("[values to insert] \n");

			for (String string : insertArr1) {

				String colName = string;
				String val = null;

				// id
				if (colName.equalsIgnoreCase("id") && prop2.getBoolean("newId")) {

					String id = "";

					if (toServerType.equals("sugar")) {

						id = "'{$id}'";

					} else if (toServerType.equals("sqlsrv")) {

						id = "NEWID()";

					} else if (toServerType.equals("mysql")) {

						id = "UUID()";

					} else if (toServerType.equals("oracle")) {

						id = "sys_guid()";

					} else {

						;

					}
					val = id + ", ";
				}

				// le date e i numeri li tratto a parte
				else if (isSqlServer2 && dbMeta1.isDateColumn(table1, colName)) {

					if (dbMeta1.isNullableColumn(table1, colName)) {
						nullColArr1.add(colName);
					}

					dateColArr1.add(colName);
					val = "{$" + colName + "}, ";

				}

				else if (dbMeta1.isNumberColumn(table1, colName)

				&& dbMeta1.isNullableColumn(table1, colName)) {

					nullColArr1.add(colName);
					val = "{$" + colName + "}, ";

				} else if (dbMeta1.isNumberColumn(table1, colName)

				&& !dbMeta1.isNullableColumn(table1, colName)) {

					notNullNumberColArr1.add(colName);
					val = "{$" + colName + "}, ";

				} else if (dbMeta1.isNullableColumn(table1, colName)) {

					nullColArr1.add(colName);
					val = "{$" + colName + "}, ";

				} else {

					val = "'{$row['" + colName + "']}', ";

				}

				values += val;

				bw1.write(val);
			}

			values = values.substring(0, values.length() - 2);

			bw1.write("\n");
		}

		if (notNullNumberColArr1.size() > 0) {

			bw1.write("[commands for not nullable number fields] \n");

			for (String string : notNullNumberColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"0\" : \"{$row['" + string
						+ "']}\";";

				bw1.write(cmd + "\n");

				notNullableNumCmd += cmd + "\n";
			}

		}

		if (nullColArr1.size() > 0) {

			bw1.write("[commands for nullable fields] \n");

			for (String string : nullColArr1) {

				String cmd = "$" + string + " = isNullOrEmptyString($row['"
						+ string + "']) ? \"NULL\" : \"'{$row['" + string
						+ "']}'\";";

				bw1.write(cmd + "\n");

				nullableCmd += cmd + "\n";
			}

		}

		if (dateColArr1.size() > 0) {

			bw1.write("[commands to convert SQL Server date fields] \n");

			for (String string : dateColArr1) {

				if (isSqlServer2) {

					String cmd = "if(!isNullOrEmptyString($row['" + string
							+ "'])) { \n"
							+ "$date = date(\"Y-m-d H:i:s\", strtotime($row['"
							+ string + "'])); \n" + "$" + string
							+ " = \"'{$date}'\"; \n" + "}";

					bw1.write(cmd + "\n");

					dateCmd += cmd + "\n";

				}

			}

		}

		String delScript = "$query = \"delete from "
				+ prop1.getString("database")
				+ "."

				+ table1
				+ "\";\n"
				+ queryStmt(toServerType, toServerConn, "$query", "$result",
						false);

		if (prop2.getBoolean("newId") && fields.indexOf("`" + "id" + "`") == -1) {

			fields = "`" + "id" + "`" + ", " + fields;

			String id = "";

			if (toServerType.equals("sugar")) {

				id = "'{$id}'";

			} else if (toServerType.equals("sqlsrv")) {

				id = "NEWID()";

			} else if (toServerType.equals("mysql")) {

				id = "UUID()";

			} else if (toServerType.equals("oracle")) {

				id = "sys_guid()";

			} else {

				;

			}
			values = id + ", " + values;
		}

		String script =

		notNullableNumCmd + "\n" + nullableCmd + "\n" + dateCmd + "\n"

		+ (toServerType.equals("sugar") ? "$id = create_guid();\n" : "")

		+ "$q=\"insert into " + prop1.getString("database") + "."

		+ table1 + "  \" . \n" + "\"( \" . \n" +

		"\"" + fields + "\" . \n" +

		"\") \" . \n" +

		"\"values \" . \n" + "\"(\" . \n" +

		"\"" + values + "\" . \n" +

		"\")\";\n" +

		queryStmt(toServerType, toServerConn, "$q", "$res", false);

		if (notInArr1.contains("team_id\n")) {

			StringTokenizer tok = new StringTokenizer(table1, "_");
			String module = "";

			while (tok.hasMoreTokens()) {
				char[] stringArray = tok.nextToken().toCharArray();
				stringArray[0] = Character.toUpperCase(stringArray[0]);
				module += new String(stringArray);
			}

			String scriptTeam =

			"\n$id = create_guid(); \n"
					+

					"$q=\"insert into "
					+ prop1.getString("database")
					+ "."

					+ "securitygroups_records \" . \n"
					+ "\"( \" . \n"
					+

					"\"`id`, `securitygroup_id`, `record_id`, `module`, `date_modified`, `modified_user_id`, `created_by`, `deleted`\" . \n"
					+

					"\") \" . \n"
					+

					"\"values \" . \n"
					+ "\"(\" . \n"
					+

					"\"'{$id}', '{$row['team_id']}', '{$row['id']}', '"
					+ module
					+ "', now(), '{$row['modified_user_id']}', '{$row['created_by']}', '{$row['deleted']}'\" . \n"
					+

					"\")\";\n" +

					queryStmt(toServerType, toServerConn, "$q", "$res", false);

			script += "\n" + scriptTeam;

			String teamDelScript = "$query = \"delete from "
					+ prop1.getString("database")
					+ ".securitygroups_records where module = '"

					+ module
					+ "'\";\n"
					+

					queryStmt(toServerType, toServerConn, "$query", "$result",
							false);

			delScript += "\n" + teamDelScript;
		}

		bw1.write("[script] \n");
		bw1.write(script);

		if (isSqlServer1) {
			renderToGenericTemplate("sqlsrv", isSqlServer2 ? "sqlsrv" : "",
					table2, table2, prop1, prop2, delScript, script, dbMeta1,
					dbMeta2);
		} else {
			renderToGenericTemplate("mysql", isSqlServer2 ? "sqlsrv" : "",
					table2, table2, prop1, prop2, delScript, script, dbMeta1,
					dbMeta2);
		}

		bw1.close();
		bw2.close();
	}

}
