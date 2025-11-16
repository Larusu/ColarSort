package com.colarsort.app.models

data class Users(val id: Integer? = null,
                 val username: String,
                 val role: String? = null,
                 val password: String)
