package com.friendalert.shivangshah.data.notifications.source

import com.friendalert.shivangshah.data.notifications.repository.NotificationDataStore
import com.friendalert.shivangshah.data.notifications.repository.NotificationRemote
import com.friendalert.shivangshah.model.notifications.response.NotificationResponseModel
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Implementation of the [BufferooDataStore] interface to provide a means of communicating
 * with the remote data source
 */
class NotificationRemoteDataStore @Inject constructor(private val notificationRemote: NotificationRemote) :
        NotificationDataStore {

    override fun getNotifications(userId: String): Single<NotificationResponseModel> {
        return notificationRemote.getNotifications(userId)
    }

    override fun getReadNotifications(): Single<ArrayList<String>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun markAsRead(notificationId: String): Single<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}