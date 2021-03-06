package com.friendalert.shivangshah.model.friends.response

import com.google.gson.annotations.SerializedName

/**
 * Created by shivangshah on 12/13/17.
 */
class CreateFriendRequestResponseModel(@SerializedName("customCode") val customCode: Int,
                                       @SerializedName("data") val data: CreateFriendRequestResponseModelData = CreateFriendRequestResponseModelData(0))

class CreateFriendRequestResponseModelData(@SerializedName("insertId") val insertId: Int)