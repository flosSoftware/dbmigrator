<?php
function getTypeInstance($type)
{
    if ($type == "mysql")
        $my_db_manager = "MysqliHandler";
        elseif ($type == "mssql")
        $my_db_manager = "MssqlHandler";
        else
            $my_db_manager = "OracleHandler";
            
            require_once ("database/{$my_db_manager}.php");
            
            if (class_exists($my_db_manager)) {
                return new $my_db_manager();
            } else {
                return null;
            }
}


function executeInsertWithLob($db, $clobData, $blobData, $sql)
{
    $db->checkConnection();
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
    $GLOBALS['log']->info("Oracle Execute: $sql");
    $stmt = oci_parse($db->database, $sql);
    if ($db->checkError("Update parse failed: $sql", false)) {
        return false;
    }
    foreach ($lob_fields as $key => $descriptor) {
        $newlob = oci_new_descriptor($db->database, OCI_D_LOB);
        oci_bind_by_name($stmt, $descriptor, $newlob, - 1, $lob_field_type[$key]);
        $lobs[$key] = $newlob;
    }
    $result = false;
    oci_execute($stmt, OCI_DEFAULT);
    if (! $db->checkError("Update execute failed: $sql", false, $stmt) && oci_num_rows($stmt)) {
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
        oci_commit($db->database);
        $result = true;
    }
    foreach ($lobs as $lob) {
        $lob->free();
    }
    oci_free_statement($stmt);
    return $result;
}

function isNullOrEmptyString($string)
{
    return (! isset($string) || trim($string) === '');
}

function _convertToUTF8_2($content)
{
    return iconv('', 'UTF8', $content);
}

function _convertToUTF8($content)
{
    if (! mb_check_encoding($content, 'UTF-8')) {
        $content = mb_convert_encoding($content, 'UTF-8', 'auto');
    }
    return $content;
}

function process_items(&$item, $key)
{
    $item = _convertToUTF8_2($item);
    // $item = _convertToUTF8($item);
    $item = trim($item);
}
