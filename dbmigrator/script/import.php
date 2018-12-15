<?php

// aggiungere a config.php
//		'ipFrom' => '127.0.0.1',
// 		'ipTo' => '127.0.0.1',
// 		'dbFrom' => 'test',
// 		'dbTo' => 'test2',
// 		'usrFrom' => 'root',
// 		'usrTo' => 'root',
// 		'pwFrom' => '',
// 		'pwTo' => '',

set_time_limit(0);
ini_set('default_charset', 'utf-8');
if(!defined('sugarEntry'))define('sugarEntry', true);
require_once ('include/entryPoint.php');

function executeInsertWithLob($db, $clobData, $blobData, $sql)
{
$db->checkConnection();
if(empty($sql)){
return false;
}
$lob_fields=array();
$lob_field_type=array();
$lobs=array();
foreach ($clobData as $field => $value) {
$lob_type = OCI_B_CLOB;
$lob_fields[$field]=":".$field;
$lob_field_type[$field]=$lob_type;
}
foreach ($blobData as $field => $value) {
$lob_type = OCI_B_BLOB;
$lob_fields[$field]=":".$field;
$lob_field_type[$field]=$lob_type;
}
if (count($lob_fields) > 0 ) {
$sql .= " RETURNING ".implode(",", array_keys($lob_fields)).' INTO '.implode(",", array_values($lob_fields));
}
$GLOBALS['log']->info("Oracle Execute: $sql");
$stmt = oci_parse($db->database, $sql);
if($db->checkError("Update parse failed: $sql", false)) {
return false;
}
foreach ($lob_fields as $key=>$descriptor) {
$newlob = oci_new_descriptor($db->database, OCI_D_LOB);
oci_bind_by_name($stmt, $descriptor, $newlob, -1, $lob_field_type[$key]);
$lobs[$key] = $newlob;
}
$result = false;
oci_execute($stmt,OCI_DEFAULT);
if(!$db->checkError("Update execute failed: $sql", false, $stmt) && oci_num_rows($stmt)) {
foreach ($lobs as $key=>$lob){
if (isset($clobData[$key])) {
$val = from_html($clobData[$key]);
} elseif (isset($blobData[$key])) {
$val = from_html($blobData[$key]);
} else {
$val = null;
}
$lob->save($val);
}
oci_commit($db->database);
$result = true;
}
foreach ($lobs as $lob){
$lob->free();
}
oci_free_statement($stmt);
return $result;
}function isNullOrEmptyString($string){
return (!isset($string) || trim($string)==='');
}
function _convertToUTF8_2($content) {
return iconv('', 'UTF8', $content);
}
function _convertToUTF8($content) {
if(!mb_check_encoding($content, 'UTF-8')) {
$content = mb_convert_encoding($content, 'UTF-8', 'auto');
}
return $content;
}
function process_items(&$item, $key)
{
$item = _convertToUTF8_2($item);
//$item = _convertToUTF8($item);
$item = trim($item);
}
function ms_escape_string($data) {
if ( isNullOrEmptyString($data) ) return $data;
if ( is_numeric($data) ) return $data;
$non_displayables = array(
'/%0[0-8bcef]/',            // url encoded 00-08, 11, 12, 14, 15
'/%1[0-9a-f]/',             // url encoded 16-31
'/[\x00-\x08]/',            // 00-08
'/\x0b/',                   // 11
'/\x0c/',                   // 12
'/[\x0e-\x1f]/'             // 14-31
);
foreach ( $non_displayables as $regex )
$data = preg_replace( $regex, '', $data );
$data = str_replace("'", "''", $data );
return $data;
}
function array_ms_escape_string($array) {
$ret = array();
foreach($array as $k => $v) {
$ret[$k] = ms_escape_string($v);
}
return $ret;
}
function array_my_escape_string($array, $conn) {
$ret = array();
foreach($array as $k => $v) {
$ret[$k] = mysqli_real_escape_string($conn, $v);
}
return $ret;
}
function isTestImport()
{
return false;
}

global $sugar_config;
global $db;
require('import/import_members.php');
