package com.natcom.model

class CloseRequest(val contract: Boolean, val mount: Boolean, val comment: String,
                   val date: String, val leadSum: String, val prepay: String, val cashless: Boolean)

class ShiftRequest(val date: String, val comment: String)
class DenyRequest(val comment: String)
