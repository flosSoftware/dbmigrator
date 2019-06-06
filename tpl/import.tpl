<?php

set_time_limit(0);
ini_set('default_charset', 'utf-8');

require_once 'utils.php';
require_once 'FlosLogger.php';

global $dbConfig;
$dbConfig = array(
    'ipFrom' => '#ipFrom#',
    'ipTo' => '#ipTo#',
	'portFrom' => '#portFrom#',
    'portTo' => '#portTo#',
    'dbFrom' => '#dbFrom#',
    'dbTo' => '#dbTo#',
    'usrFrom' => '#usrFrom#',
    'usrTo' => '#usrTo#',
    'pwFrom' => '#pwFrom#',
    'pwTo' => '#pwTo#'
);

global $logger;
$logger = new FlosLogger('import.log');
global #dbToHandlerName#;
#dbToHandlerName# = getTypeInstance("#dbToType#");
global #dbFromHandlerName#;
#dbFromHandlerName# = getTypeInstance("#dbFromType#");
