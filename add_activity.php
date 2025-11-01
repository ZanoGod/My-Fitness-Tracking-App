<?php
// add_activity.php
// This script adds a new workout to the database.

include 'db.php';

header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

$response = array();

// Check for required fields
if (isset($_POST['user_id']) && isset($_POST['activity_type']) && isset($_POST['duration_minutes'])) {

    // Get required data
    $user_id = $_POST['user_id'];
    $activity_type = $_POST['activity_type'];
    $duration_minutes = $_POST['duration_minutes'];

    // Get optional data (for running, cycling, weightlifting)
    // Use "null" if the data is not sent or is empty
    $distance_km = isset($_POST['distance_km']) && !empty($_POST['distance_km']) ? $_POST['distance_km'] : NULL;
    $weight_kg = isset($_POST['weight_kg']) && !empty($_POST['weight_kg']) ? $_POST['weight_kg'] : NULL;
    $sets = isset($_POST['sets']) && !empty($_POST['sets']) ? $_POST['sets'] : NULL;
    $reps = isset($_POST['reps']) && !empty($_POST['reps']) ? $_POST['reps'] : NULL;

    // Prepare the insert statement
    $stmt = $conn->prepare(
        "INSERT INTO activities (user_id, activity_type, duration_minutes, distance_km, weight_kg, sets, reps, activity_date) 
         VALUES (?, ?, ?, ?, ?, ?, ?, NOW())"
    );
    
    // "isssiii" maps to the variable types:
    // i = integer (user_id)
    // s = string (activity_type)
    // i = integer (duration_minutes)
    // s = string (distance_km - it's a decimal, but "s" works well with bind_param for NULLs)
    // s = string (weight_kg)
    // i = integer (sets)
    // i = integer (reps)
    $stmt->bind_param("issssii", 
        $user_id, 
        $activity_type, 
        $duration_minutes, 
        $distance_km, 
        $weight_kg, 
        $sets, 
        $reps
    );

    // Execute the statement
    if ($stmt->execute()) {
        $response['status'] = 'success';
        $response['message'] = 'Activity added successfully.';
    } else {
        $response['status'] = 'error';
        $response['message'] = 'Failed to add activity: ' . $stmt->error;
    }
    $stmt->close();

} else {
    $response['status'] = 'error';
    $response['message'] = 'Required fields (user_id, activity_type, duration_minutes) are missing.';
}

$conn->close();
echo json_encode($response);
?>
