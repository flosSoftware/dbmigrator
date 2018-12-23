<?php
function getTypeInstance($type)
{
    if ($type == "mysql")
        $my_db_manager = "MysqliHandler";
        elseif ($type == "sqlsrv")
        $my_db_manager = "SqlsrvHandler";
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

function _convertToUTF8_3($content)
{
    return iconv('', 'UTF-8', $content);
}

function _convertToUTF8_2($content)
{
    $massaged = htmlentities (utf8_encode($content), ENT_QUOTES, "UTF-8");
    //$unmassaged = utf8_decode(html_entity_decode($massaged, ENT_QUOTES, "UTF-8"));
    return $massaged;
}

function _convertToUTF8($content)
{
    if (!mb_check_encoding($content, 'UTF-8') ||
        !($content === mb_convert_encoding(mb_convert_encoding($content, 'UTF-32', 'UTF-8'), 'UTF-8', 'UTF-32'))
        ) {
            $content = mb_convert_encoding($content, 'UTF-8');
        }
        return $content;
}

function processItems(&$item, $key)
{
    $item = _convertToUTF8_3($item);
    //$item = _convertToUTF8($item);
    $item = trim($item);
}

function ensureLength(&$string, $length)
{
    $strlen = strlen($string);
    if($strlen < $length)
    {
        $string = str_pad($string,$length,"0");
    }
    else if($strlen > $length)
    {
        $string = substr($string, 0, $length);
    }
}

function createGuid()
{
    $microTime = microtime();
    list($a_dec, $a_sec) = explode(" ", $microTime);
    $dec_hex = dechex($a_dec* 1000000);
    $sec_hex = dechex($a_sec);
    ensureLength($dec_hex, 5);
    ensureLength($sec_hex, 6);
    $guid = "";
    $guid .= $dec_hex;
    $guid .= createGuidSection(3);
    $guid .= '-';
    $guid .= createGuidSection(4);
    $guid .= '-';
    $guid .= createGuidSection(4);
    $guid .= '-';
    $guid .= createGuidSection(4);
    $guid .= '-';
    $guid .= $sec_hex;
    $guid .= createGuidSection(6);
    return $guid;
}

function createGuidSection($characters)
{
    $return = "";
    for($i=0; $i<$characters; $i++)
    {
        $return .= dechex(mt_rand(0,15));
    }
    return $return;
}

function startsWith($haystack, $needle) {
    // search backwards starting from haystack length characters from the end
    return $needle === "" || strrpos($haystack, $needle, -strlen($haystack)) !== FALSE;
}

function endsWith($haystack, $needle) {
    // search forward starting from end minus needle length characters
    return $needle === "" || (($temp = strlen($haystack) - strlen($needle)) >= 0 && strpos($haystack, $needle, $temp) !== FALSE);
}

function isTestImport() {
    return true;
}

