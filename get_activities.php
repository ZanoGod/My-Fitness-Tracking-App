<?php
// get_activities.php
// This script gets all activities for a specific user.

include 'db.php';

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$response = array();

// Check if user_id was sent (using GET method this time)
if (isset($_GET['user_id'])) {
    
    $user_id = $_GET['user_id'];

    // Prepare statement to select activities
    $stmt = $conn->prepare("SELECT activity_id, activity_type, duration_minutes, distance_km, weight_kg, sets, reps, activity_date 
                            FROM activities 
                            WHERE user_id = ? 
                            ORDER BY activity_date DESC"); // Order by date, newest first
    
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    $activities = array();

    // Loop through all the results
    while ($row = $result->fetch_assoc()) {
        // Add each row (activity) to the $activities array
        $activities[] = $row;
    }

    // Send a success response with the list of activities
    $response['status'] = 'success';
    $response['activities'] = $activities;
    
    $stmt->close();

} else {
    $response['status'] = 'error';
    $response['message'] = 'User ID is required.';
}

$conn->close();
echo json_encode($response);
?>
