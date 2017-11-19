package com.friendalert.shivangshah.remote.user

import com.google.gson.annotations.SerializedName

/**
 * Created by shivangshah on 11/17/17.
 */
class UserRequestModel (@SerializedName("body") val body: Body)

class Body(@SerializedName("userId") val userId: String,
           @SerializedName("firstName") val firstName: String,
           @SerializedName("lastName") val lastName: String,
           @SerializedName("phoneNumber") val phoneNumber: String,
           @SerializedName("pushNotificationToken") val pushNotificationToken: String,
           @SerializedName("oldpushNotificationToken") val oldpushNotificationToken: String,
           @SerializedName("active") val active: Int)