<?php 

class FlosLogger {
    
    private $filePath;
    
    public function __construct($afilePath) {
        
        $this->filePath = $afilePath;
    }

    public function log($var) {
    	if(is_bool($var)) {
    		
    		if($var) $var = "true";
    		else $var = "false";
     	}
        $a = array_shift(debug_backtrace(DEBUG_BACKTRACE_PROVIDE_OBJECT, 1));
        $p = print_r($var, true);
        error_log('[' . basename($a['file']) . ' - line ' . $a['line'] . ']' . PHP_EOL . $p . PHP_EOL, 3, $this->filePath); 
        
    }

}

 ?>