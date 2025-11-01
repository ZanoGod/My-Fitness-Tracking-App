<?php
// register.php
// This script handles new user registration.

// Include the database connection file
include 'db.php';

// Allow cross-origin requests (for testing from the app)
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Create an array to store the response
$response = array();

// Check if the required data was sent via POST
if (isset($_POST['username']) && isset($_POST['email']) && isset($_POST['password'])) {

    // Get data from the POST request
    $username = $_POST['username'];
    $email = $_POST['email'];
    $password = $_POST['password'];

    // --- Validation (Simple) ---
    if (empty($username) || empty($email) || empty($password)) {
        $response['status'] = 'error';
        $response['message'] = 'All fields are required.';
        echo json_encode($response);
        exit(); // Stop the script
    }

    // --- Check if email already exists ---
    // Use a prepared statement to prevent SQL injection
    $stmt = $conn->prepare("SELECT user_id FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    if ($stmt->num_rows > 0) {
        // Email already exists
        $response['status'] = 'error';
        $response['message'] = 'This email is already registered.';
    } else {
        // Email is available, now create the user

        // --- Hash the password ---
        // NEVER store plain text passwords. Use password_hash.
        $password_hash = password_hash($password, PASSWORD_DEFAULT);

        // --- Insert the new user ---
        $stmt_insert = $conn->prepare("INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)");
        $stmt_insert->bind_param("sss", $username, $email, $password_hash);

        if ($stmt_insert->execute()) {
            // Registration successful
            $response['status'] = 'success';
            $response['message'] = 'User registered successfully.';
        } else {
            // Registration failed
            $response['status'] = 'error';
            $response['message'] = 'Registration failed: ' . $stmt_insert->error;
        }
        $stmt_insert->close();
    }
    $stmt->close();

} else {
    // Required data was not sent
    $response['status'] = 'error';
    $response['message'] = 'Required fields are missing.';
}

// Close the database connection
$conn->close();

// Send the response back to the app as JSON
echo json_encode($response);
?>
