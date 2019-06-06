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

$query = "delete from [{$dbConfig['dbTo']}].[dbo].users";
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



$email = isNullOrEmptyString($row['email']) ? "NULL" : "'{$row['email']}'";


$q="insert into [{$dbConfig['dbTo']}].[dbo].users  " . 
"( " . 
"[id], [user_name]" . 
") " . 
"values " . 
"(" . 
"NEWID(), {$email}" . 
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