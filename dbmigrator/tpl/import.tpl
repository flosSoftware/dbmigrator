<?php

set_time_limit(0);
ini_set('default_charset', 'utf-8');

require_once 'utils.php';
require_once 'FlosLogger.php';

global $dbConfig = array(
    'ipFrom' => '#ipFrom#',
    'ipTo' => '#ipTo#',
    'dbFrom' => '#dbFrom#',
    'dbTo' => '#dbTo#',
    'usrFrom' => '#usrFrom#',
    'usrTo' => '#usrTo#',
    'pwFrom' => '#pwFrom#',
    'pwTo' => '#pwTo#'
);

global $logger = new FlosLogger('import.log');
global #dbToHandlerName# = getTypeInstance("#dbToType#");
global #dbFromHandlerName# = getTypeInstance("#dbFromType#");
