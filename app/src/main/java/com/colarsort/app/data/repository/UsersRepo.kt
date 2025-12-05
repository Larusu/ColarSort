package com.colarsort.app.data.repository

import com.colarsort.app.data.db.dao.UserDao
import com.colarsort.app.data.entities.Users
import com.colarsort.app.data.pojo.UserIdAndRole
import com.colarsort.app.data.pojo.PublicUser
import com.colarsort.app.utils.UtilityHelper.hashPassword

class UsersRepo (private val dao: UserDao)
{
    /**
     * Returns userId if the user exists on the existing accounts
     *
     * @param username username of the user performing action
     * @param password password of the user performing action
     */
    suspend fun getIdAndRoleIfExists(username: String, password: String): UserIdAndRole =
        dao.getIdAndRole(username, hashPassword(password))

    /**
     * Creates a new worker account by admin and
     * returns true if creating is successful
     *
     * @param newEmployeeUsername username of the new worker account
     * @param employeePassword password of the new worker account
     *
     * @return returns true if delete is successful
     */
    suspend fun assignWorker(newEmployeeUsername: String, employeePassword: String): Boolean
    {
        val user = Users(
            id = 0,
            username = newEmployeeUsername,
            role = "Worker",
            password = hashPassword(employeePassword)
        )
        val result = dao.addWorker(user)

        return result != -1L // returns true if insert of row ID is successful
    }

    /**
     * Retrieves all users from the database, ordered first by role priority and
     * then by username
     *
     * Role ordering is defined as:
     * - Manager -> Highest priority
     * - Worker  -> Lowest priority
     *
     * @return A list of users that is sorted by role priority and username
     */
    suspend fun getAll() : List<PublicUser> = dao.getUsersData()

    /**
     * Deletes a user based on ID
     *
     * @return returns true if delete is successful
     */
    suspend fun deleteUser(userId: Int): Boolean =
        dao.delete(userId) > 0

    suspend fun initializeUsers()
    {
        val admin = Users(
            id = 0,
            username = "admin",
            role = "Manager",
            password = hashPassword("admin")
        )

        val user = Users(
            id = 0,
            username = "user",
            role = "Worker",
            password = hashPassword("user")
        )

        dao.insert(admin)
        dao.insert(user)
    }
}