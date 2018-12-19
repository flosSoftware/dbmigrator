<?php

global $logger;
global $toDBHandler;
global $fromDBHandler;

$logger->fatal('Start import members');

$fromDBHandler->connect($dbConfig['ipFrom'], $dbConfig['usrFrom'], $dbConfig['pwFrom'], $dbConfig['dbFrom']);

$toDBHandler->connect($dbConfig['ipFrom'], $dbConfig['usrFrom'], $dbConfig['pwFrom'], $dbConfig['dbFrom']);



$query = "delete from {$dbConfig['dbTo']}.users";
$result = $toDBHandler->query($query, isTestImport());



$query = "select * from members";

if(isTestImport()) $query = $fromDBHandler->getLimitedQuery($query);

$result = $fromDBHandler->query($query, isTestImport());

while ($row = $fromDBHandler->fetchRow($result)) {

$row = $toDBHandler->arrayQuote($row);

array_walk_recursive($row, 'process_items');



$full_name = isNullOrEmptyString($row['full_name']) ? "NULL" : "'{$row['full_name']}'";
$login_id = isNullOrEmptyString($row['login_id']) ? "NULL" : "'{$row['login_id']}'";


$q="insert into {$dbConfig['dbTo']}.users  " . 
"( " . 
"`id`, `last_name`, `user_name`" . 
") " . 
"values " . 
"(" . 
"UUID(), {$full_name}, {$login_id}" . 
")";
$res = $toDBHandler->query($q, isTestImport());



}

$fromDBHandler->disconnect();

$toDBHandler->disconnect();

$logger->fatal('End import members');