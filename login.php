<?php
// login.php
// This script handles user login.

include 'db.php';

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$response = array();

// Check if email and password were sent
if (isset($_POST['email']) && isset($_POST['password'])) {
    
    $email = $_POST['email'];
    $password = $_POST['password'];

    // Prepare statement to find the user
    $stmt = $conn->prepare("SELECT user_id, username, password_hash FROM users WHERE email = ?");
    $stmt->bind_param("s", $email);
    $stmt->execute();
    $stmt->store_result();

    // Check if a user with that email was found
    if ($stmt->num_rows > 0) {
        // User found, bind the results
        $stmt->bind_result($user_id, $username, $password_hash);
        $stmt->fetch();

        // --- Verify the password ---
        // Compare the submitted password with the hash from the database
        if (password_verify($password, $password_hash)) {
            // Password is correct!
            $response['status'] = 'success';
            $response['message'] = 'Login successful.';
            // Send user data back to the app
            $response['user'] = array(
                'user_id' => $user_id,
                'username' => $username,
                'email' => $email
            );
        } else {
            // Incorrect password
            $response['status'] = 'error';
            $response['message'] = 'Invalid email or password.';
        }
    } else {
        // No user found with that email
        $response['status'] = 'error';
        $response['message'] = 'Invalid email or password.';
    }
    $stmt->close();
} else {
    $response['status'] = 'error';
    $response['message'] = 'Email and password are required.';
}

$conn->close();
echo json_encode($response);
?>
