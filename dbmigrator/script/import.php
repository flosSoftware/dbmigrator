<?php

set_time_limit(0);
ini_set('default_charset', 'utf-8');

require_once 'utils.php';
require_once 'FlosLogger.php';

global $dbConfig;
$dbConfig = array(
    'ipFrom' => 'localhost',
    'ipTo' => 'localhost',
	'portFrom' => '3306',
    'portTo' => '1434',
    'dbFrom' => 'test',
    'dbTo' => 'test',
    'usrFrom' => 'root',
    'usrTo' => 'sa',
    'pwFrom' => '',
    'pwTo' => 'ForzaSql.3'
);

global $logger;
$logger = new FlosLogger('import.log');
global $toDBHandler;
$toDBHandler = getTypeInstance("sqlsrv");
global $fromDBHandler;
$fromDBHandler = getTypeInstance("mysql");
require('import/import_members.php');
