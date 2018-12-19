<?php

set_time_limit(0);
ini_set('default_charset', 'utf-8');

require_once 'utils.php';
require_once 'FlosLogger.php';

global $dbConfig = array(
    'ipFrom' => '127.0.0.1',
    'ipTo' => '127.0.0.1',
    'dbFrom' => 'test',
    'dbTo' => 'test2',
    'usrFrom' => 'root',
    'usrTo' => 'root',
    'pwFrom' => '',
    'pwTo' => ''
);

global $logger = new FlosLogger('import.log');
global $toDBHandler = getTypeInstance("mysql");
global $fromDBHandler = getTypeInstance("mysql");
require('import/import_members.php');
