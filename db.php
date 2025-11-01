<?php
// db.php
// This file connects to your MySQL database.

// Database credentials
$servername = "localhost"; // This is "localhost" because you are using XAMPP
$username = "root";        // Default username for XAMPP
$password = "";            // Default password for XAMPP is empty
$dbname = "fitness_tracking_app"; // The database name you created

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);

// Check connection
if ($conn->connect_error) {
    // If connection fails, stop the script and show the error.
    die("Connection failed: " . $conn->connect_error);
}

// Set the character set to utf8mb4 for full UTF-8 support
$conn->set_charset("utf8mb4");

// This file does not output anything if the connection is successful.
// Other files will "include" this file to get the $conn variable.
?>
