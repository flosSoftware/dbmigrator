<?php

class SqlsrvHandler extends DBHandler
{

    public function getLimitedQuery($q) 
    {
        if(starts_with(strtolower($q),"select ") && $isTest)
            $q = str_ireplace("select ", "select TOP 10 ", $q, 1);
        return $q;
    }
    
    public function query($q)
    {
        $this->lastResult = sqlsrv_query($this->connection, $q);
        return $this->lastResult
    }

    public function getRowCount($result)
    {
        return null;
    }

    public function freeDbResult($dbResult)
    {
        if (! empty($dbResult))
            sqlsrv_free_result($dbResult);
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
            sqlsrv_close($this->connection);
            $this->connection = null;
        }
    }

    public function fetchRow($result)
    {
        if (empty($result))
            return false;
        
            $row = sqlsrv_fetch_array($result, SQLSRV_FETCH_ASSOC);
        
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
        if (isNullOrEmptyString($string))
            return $string;
        if (is_numeric($string))
            return $string;
        $non_displayables = array(
            '/%0[0-8bcef]/', // url encoded 00-08, 11, 12, 14, 15
            '/%1[0-9a-f]/', // url encoded 16-31
            '/[\x00-\x08]/', // 00-08
            '/\x0b/', // 11
            '/\x0c/', // 12
            '/[\x0e-\x1f]/' // 14-31
        );
        foreach ($non_displayables as $regex)
            $string = preg_replace($regex, '', $string);
        $string = str_replace("'", "''", $string);
        return $string;
    }

    public function connect($serverIP, $serverPort, $serverUser, $serverPw, $dbName)
    {
        $conn = sqlsrv_connect($serverIP, array(
            "Database" => $dbName,
            "UID" => $serverUser,
            "PWD" => $serverPw,
            "CharacterSet" => "UTF-8",
            "ReturnDatesAsStrings" => true,
            "MultipleActiveResultSets" => true
        )) or die(print_r(sqlsrv_errors(), true));
        
        $this->connection = $conn;
    }

    public function getLastError()
    {
        return sqlsrv_errors($this->connection);
    }
}
