package com.zyproo.devlink.common

enum class Role {
    STUDENT, TEACHER, ADMIN, RECRUITER;

    companion object {
        fun fromString(value: String): Role =
            entries.firstOrNull { it.name == value }
                ?: throw IllegalArgumentException("Unknown role from server: $value")
    }
}
