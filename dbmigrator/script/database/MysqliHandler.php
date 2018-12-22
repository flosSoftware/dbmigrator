<?php
require_once('database/DBHandler.php');
class MysqliHandler extends DBHandler
{
    public function getLimitedQuery($q)
    {
        if(startsWith(strtolower($q),"select "))
            $q = $q . " LIMIT 0,10";
        return $q;
    }
    
    public function query($q)
    {   
        $this->lastResult = mysqli_query($this->connection,$q);
        return $this->lastResult;        
    }

    public function getRowCount($result)
    {
        return mysqli_num_rows($result);
    }

    
    public function freeDbResult($dbResult)
    {
        if(!empty($dbResult))
            mysqli_free_result($dbResult);
    }
    
    public function freeLastResult()
    {
        $this->freeDbResult($this->lastResult);
        $this->lastResult = null;
    }
    
    public function disconnect()
    {
        if (! empty($this->connection)) {
            $this->freeLastResult();
            mysqli_close($this->connection);
            $this->connection = null;
        }
    }

    public function fetchRow($result)
    {
        if (empty($result))
            return false;
        
        $row = mysqli_fetch_array($result);
        
        if ($row == null)
            $row = false; 
        
        return $row;
    }

    public function arrayQuote($array)
    {
        $ret = array();
        foreach ($array as $k => $v) {
            $ret[$k] = $this->quote($v);
        }
        return $ret;
    }

    public function quote($string)
    {
        return mysqli_real_escape_string($this->connection, $string);
    }

    public function connect($serverIP, $serverPort, $serverUser, $serverPw, $dbName)
    {
        $conn = mysqli_connect($serverIP, $serverUser, $serverPw, $dbName);
        
        if (mysqli_connect_errno()) {
            die(print_r(mysqli_connect_error(), true));
        } else
            $this->connection = $conn;
    }
    
    public function getLastError() {
        
        return mysqli_error($this->connection);
    }
}
