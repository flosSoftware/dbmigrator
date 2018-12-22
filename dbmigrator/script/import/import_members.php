<?php

require_once 'utils.php';

global $dbConfig;
global $logger;
global $toDBHandler;
global $fromDBHandler;

$logger->log('Start import members');

$fromDBHandler->connect($dbConfig['ipFrom'], $dbConfig['portFrom'], $dbConfig['usrFrom'], $dbConfig['pwFrom'], $dbConfig['dbFrom']);

$toDBHandler->connect($dbConfig['ipTo'], $dbConfig['portTo'], $dbConfig['usrTo'], $dbConfig['pwTo'], $dbConfig['dbTo']);



$result = $toDBHandler->query("SET DATEFORMAT ymd");

if(!$result) {
	$logger->log("Query error for: " . $q);
	$logger->log("Error description: " . $toDBHandler->getLastError());
}

$query = "delete from [{$dbConfig['dbTo']}].[dbo].members";
$result = $toDBHandler->query($query);

if(!$result) {
	$logger->log("Query error for: " . $q);
	$logger->log("Error description: " . $toDBHandler->getLastError());
}



$query = "select * from {$dbConfig['dbFrom']}.members";

if(isTestImport()) $query = $fromDBHandler->getLimitedQuery($query);

$result = $fromDBHandler->query($query);

if(!$result) {
	$logger->log("Query error for: " . $q);
	$logger->log("Error description: " . $fromDBHandler->getLastError());
}

while ($row = $fromDBHandler->fetchRow($result)) {

$row = $toDBHandler->arrayQuote($row);

array_walk_recursive($row, 'processItems');



$email = isNullOrEmptyString($row['email']) ? "''" : "'{$row['email']}'";
$full_name = isNullOrEmptyString($row['full_name']) ? "''" : "'{$row['full_name']}'";
$login_id = isNullOrEmptyString($row['login_id']) ? "''" : "'{$row['login_id']}'";
$passwd = isNullOrEmptyString($row['passwd']) ? "''" : "'{$row['passwd']}'";


$q="insert into [{$dbConfig['dbTo']}].[dbo].members  " . 
"( " . 
"[id], [email], [full_name], [login_id], [passwd]" . 
") " . 
"values " . 
"(" . 
"NEWID(), {$email}, {$full_name}, {$login_id}, {$passwd}" . 
")";
$res = $toDBHandler->query($q);

if(!$res) {
	$logger->log("Query error for: " . $q);
	$logger->log("Error description: " . $toDBHandler->getLastError());
}



}

$fromDBHandler->disconnect();

$toDBHandler->disconnect();

$logger->log('End import members');