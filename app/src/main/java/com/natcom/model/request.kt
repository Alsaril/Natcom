package com.natcom.model

data class LoginRequest(val login: String, val password: String)
data class CloseRequest(val contract: Boolean, val mount: Boolean, val comment: String, val date: String)
data class ShiftRequest(val comment: String, val date: String)
data class DenyRequest(val comment: String)
