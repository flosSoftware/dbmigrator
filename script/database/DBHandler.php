<?php

require_once 'utils.php';

abstract class DBHandler
{

	protected $connection;	
	protected $lastResult;

	
	public function __construct()
	{
	    $this->connection = null;
	    $this->lastResult = null;
	}
	
	abstract public function getLimitedQuery($q);

	abstract public function query($q);
	
// 	abstract public function getRowCount($result);	
	
// 	abstract public function freeDbResult($dbResult);
	
// 	abstract public function freeLastResult();
	
	abstract public function disconnect();
	
	abstract public function fetchRow($result);
	
	abstract public function arrayQuote($array);
	
	abstract public function quote($string);
	
	abstract public function connect($serverIP, $serverPort, $serverUser, $serverPw, $dbName);
	
	abstract public function getLastError();
}
