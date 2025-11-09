package com.mad.myfitnesstrackingapp.networks

object ApiConfig {
    const val SERVER_IP = "192.168.1.2"
    const val BASE = "http://$SERVER_IP/fitness_api"
    const val LOGIN_URL = "$BASE/login.php"
    const val REGISTER_URL = "$BASE/register.php"
    const val GET_ACTIVITIES_URL = "$BASE/get_activities.php"
    const val ADD_ACTIVITY_URL = "$BASE/add_activity.php"
}
