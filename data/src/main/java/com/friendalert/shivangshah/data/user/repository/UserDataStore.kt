package com.friendalert.shivangshah.data.user.repository

import com.friendalert.shivangshah.data.user.UserEntity
import com.friendalert.shivangshah.data.user.UserResponseModelEntity
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created by shivangshah on 11/17/17.
 */
interface UserDataStore {

    fun createUser(user: UserEntity) : Single<UserResponseModelEntity>

    fun updateUser(user: UserEntity) : Single<UserResponseModelEntity>

    fun logoutUser(user: UserEntity) : Single<UserResponseModelEntity>

    fun getUser() : Single<UserEntity>

}