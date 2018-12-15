<?php
if(!defined('sugarEntry'))define('sugarEntry', true);
require_once 'include/entryPoint.php';

$GLOBALS['log']->fatal('Start import members');

$fromMySQLConn = mysqli_connect($sugar_config['ipFrom'],$sugar_config['usrFrom'],$sugar_config['pwFrom'],$sugar_config['dbFrom']);

if (mysqli_connect_errno()) { die(print_r(mysqli_connect_error(), true)); }

$toMySQLConn = mysqli_connect($sugar_config['ipTo'],$sugar_config['usrTo'],$sugar_config['pwTo'],$sugar_config['dbTo']);

if (mysqli_connect_errno()) { die(print_r(mysqli_connect_error(), true)); }



$query = "delete from {$sugar_config['dbTo']}.users";
$result = mysqli_query($toMySQLConn,$query);

if($result === false) {
$GLOBALS['log']->fatal(mysqli_error($toMySQLConn));
}



$query = "select * from {$sugar_config['dbFrom']}.members " . (isTestImport() ? "LIMIT 0, 10" : "")."";

$result = mysqli_query($fromMySQLConn,$query);

if($result === false) {
$GLOBALS['log']->fatal(mysqli_error($fromMySQLConn));
}

while ($row = mysqli_fetch_array($result)) {

$row = array_my_escape_string($row,$toMySQLConn);

array_walk_recursive($row, 'process_items');



$full_name = isNullOrEmptyString($row['full_name']) ? "NULL" : "'{$row['full_name']}'";
$login_id = isNullOrEmptyString($row['login_id']) ? "NULL" : "'{$row['login_id']}'";


$q="insert into {$sugar_config['dbTo']}.users  " . 
"( " . 
"`last_name`, `user_name`" . 
") " . 
"values " . 
"(" . 
"{$full_name}, {$login_id}" . 
")";
$res = mysqli_query($toMySQLConn,$q);

if($res === false) {
$GLOBALS['log']->fatal(mysqli_error($toMySQLConn));
}



}

mysqli_close($fromMySQLConn);

mysqli_close($toMySQLConn);

$GLOBALS['log']->fatal('End import members');