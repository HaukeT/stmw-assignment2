<!DOCTYPE html>
<html>
<body>

<form method="post" action="<?php echo $_SERVER['PHP_SELF'];?>">
   Search Text: <input type="text" name="searchText">
   <!-- Search Field: <input type="text" name="searchField"> -->
   <!-- <input type="submit"> -->
   <input type="submit" name="textSearch" alt="text search" value=""/>
   <br>
   Width: <input type="text" name="width">  myLatitude: <input type="text" name="centerX"> myLongitude: <input type="text" name="centerY">
   <input type="submit" name="spatialSearch" alt="spatial search" value=""/>
</form>

<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
  if (isset($_POST['textSearch'])){
    // collect value of input field
    $searchText = format_input($_POST['searchText']);
   //  $searchField = format_input($_POST['searchField']);

    if (empty($searchText)) {
        echo "Search text is empty";
    } else {
      echo $searchText;
      echo "<br>";
        $output = shell_exec("./java -cp /usr/share/java/mysql-connector-java-5.1.28.jar:/usr/share/java/lucene-core-5.4.0.jar:/usr/share/java/lucene-analyzers-common-5.4.0.jar:/usr/share/java/lucene-queryparser-5.4.0.jar:/usr/share/java/lucene-queries-5.4.0.jar:. Searcher"." ".$searchText);
        //It is totally unsafe to do this in a real website, usually it is unacceptable to generate the shell command by user input, it is way too easy to hack. The only reason why we are doing so here is that we are pretty sure it will only be run locally and it is an easy way to run a java program from a web page without wrapping it.
        echo $output;
    }
  }
  elseif (isset($_POST['spatialSearch'])){
    $centerX = format_input($_POST['centerX']);
    $centerY = format_input($_POST['centerY']);
    $width = format_input($_POST['width']);

    if (empty($centerX)||empty($centerY)||empty($width)) {
        echo "width or myLatitude or myLongitude is empty";
    } else {
      echo $centerX." ".$centerY." ".$width;
      echo "<br>";
        $output = shell_exec("./java -cp /usr/share/java/mysql-connector-java-5.1.28.jar:/usr/share/java/lucene-core-5.4.0.jar:/usr/share/java/lucene-analyzers-common-5.4.0.jar:/usr/share/java/lucene-queryparser-5.4.0.jar:/usr/share/java/lucene-queries-5.4.0.jar:. Searcher"." ".$searchText." -w ".$width." -x ".$centerX." -y ".$centerY);
        //It is totally unsafe to do this in a real website, usually it is unacceptable to generate the shell command by user input, it is way too easy to hack. The only reason why we are doing so here is that we are pretty sure it will only be run locally and it is an easy way to run a java program from a web page without wrapping it.
        echo $output;
    }
  }
}

function format_input($data) {
  $data = trim($data);
  $data = stripslashes($data);
  $data = htmlspecialchars($data);
  return $data;
}
?>

</body>
</html>
