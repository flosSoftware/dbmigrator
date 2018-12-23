<?php

require_once('database/DBHandler.php');

class OracleHandler extends DBHandler
{
    
    
    public function executeInsertWithLob($clobData, $blobData, $sql)
    {
        if (empty($sql)) {
            return false;
        }
        $lob_fields = array();
        $lob_field_type = array();
        $lobs = array();
        foreach ($clobData as $field => $value) {
            $lob_type = OCI_B_CLOB;
            $lob_fields[$field] = ":" . $field;
            $lob_field_type[$field] = $lob_type;
        }
        foreach ($blobData as $field => $value) {
            $lob_type = OCI_B_BLOB;
            $lob_fields[$field] = ":" . $field;
            $lob_field_type[$field] = $lob_type;
        }
        if (count($lob_fields) > 0) {
            $sql .= " RETURNING " . implode(",", array_keys($lob_fields)) . ' INTO ' . implode(",", array_values($lob_fields));
        }
        $stmt = oci_parse($this->conn, $sql);
        if ($db->checkError($stmt)) {
            return false;
        }
        foreach ($lob_fields as $key => $descriptor) {
            $newlob = oci_new_descriptor($this->conn, OCI_D_LOB);
            oci_bind_by_name($stmt, $descriptor, $newlob, - 1, $lob_field_type[$key]);
            $lobs[$key] = $newlob;
        }
        $result = false;
        oci_execute($stmt, OCI_DEFAULT);
        if (!$db->checkError($stmt) && oci_num_rows($stmt)) {
            foreach ($lobs as $key => $lob) {
                if (isset($clobData[$key])) {
                    $val = from_html($clobData[$key]);
                } elseif (isset($blobData[$key])) {
                    $val = from_html($blobData[$key]);
                } else {
                    $val = null;
                }
                $lob->save($val);
            }
            oci_commit($this->conn);
            $result = true;
        }
        foreach ($lobs as $lob) {
            $lob->free();
        }
        oci_free_statement($stmt);
        return $result;
    }
    
    
    public function getLimitedQuery($q)
    {
        if(startsWith(strtolower($q),"select "))
            $q = $q . " LIMIT 0,10";
            return $q;
    }
    
    public function query($q)
    {
        $result = false;
        
        $stmt = oci_parse($this->conn, $q);
        if(!$this->checkError($stmt)) {
           
            if(oci_execute($stmt)) {
                $result = $stmt;
            }
        }
        
        $this->lastResult = $result;
        return $this->lastResult;
    }
    
    //public function getRowCount($result) {}
    
    public function getRowCount($query)
    {
        $count_query = 'SELECT COUNT(*) AS NUMBER_OF_ROWS FROM (' . $query . ')';
        
        $stmt = oci_parse($this->conn, $count_query);
        
        oci_define_by_name($stmt, 'NUMBER_OF_ROWS', $number_of_rows);
        
        oci_execute($stmt);
        
        oci_fetch($stmt);
        
        return $number_of_rows;
    }
    
    public function freeDbResult($dbResult)
    {
        if(!empty($dbResult))
            oci_free_statement($dbResult);
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
            oci_close($this->connection);
            $this->connection = null;
        }
    }
    

    
    public function fetchRow($result)
    {
        if (empty($result))	return false;
        
        $row = oci_fetch_array($result, OCI_ASSOC|OCI_RETURN_NULLS|OCI_RETURN_LOBS);
        if ( !$row )
            return false;
            if (!$this->checkError($result)) {
                $temp = $row;
                $row = array();
                foreach ($temp as $key => $val)
                    // make the column keys as lower case. Trim the val returned
                    $row[strtolower($key)] = trim($val);
            }
            else
                return false;
                
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
        return str_replace("'", "''", $string);
    }
    
    public function connect($serverIP, $serverPort, $serverUser, $serverPw, $dbName)
    {
        $conn = oci_connect($serverUser, $serverPw, $serverIP . ':' . $serverPort . '/XE', 'AL32UTF8')
            or die(print_r(oci_error(), true));
        
        $this->connection = $conn;
    }
    
    public function getLastError()
    {
        return checkError($this->lastResult);
    }
    
    public function checkError($stmt) {
        
        if(empty($stmt)) return false;
        
        $err = oci_error($stmt);
        if ($err){
            $error = $err['code']."-".$err['message'];
            return $error;
        }
        return false;
    }
}
