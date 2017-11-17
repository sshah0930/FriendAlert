package com.friendalert.shivangshah.data.notifications

import com.friendalert.shivangshah.data.notifications.source.NotificationDataStoreFactory
import com.friendalert.shivangshah.data.notifications.source.NotificationRemoteDataStore
import com.friendalert.shivangshah.domain.notifications.Notification
import com.friendalert.shivangshah.domain.notifications.NotificationRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

/**
 * Provides an implementation of the [BufferooRepository] interface for communicating to and from
 * data sources
 */
class NotificationDataRepository @Inject constructor(private val factory: NotificationDataStoreFactory,
                                                 private val notificationMapper: NotificationMapper) :
        NotificationRepository {
    override fun clearNotifications(): Completable {
        return factory.retrieveCacheDataStore().clearNotifications()
    }

    override fun saveNotifications(notifications: List<Notification>): Completable {
        val notificationEntities = notifications.map { notificationMapper.mapToEntity(it) }
        return saveNotificationEntities(notificationEntities)
    }

    override fun getNotifications(): Single<List<Notification>> {
        val dataStore = factory.retrieveDataStore()
        return dataStore.getNotifications()
                .flatMap {
                    if (dataStore is NotificationRemoteDataStore) {
                        saveNotificationEntities(it).toSingle { it }
                    } else {
                        Single.just(it)
                    }
                }
                .map { list ->
                    list.map { listItem ->
                        notificationMapper.mapFromEntity(listItem)
                    }
                }
    }

    private fun saveNotificationEntities(notifications: List<NotificationEntity>): Completable {
        return factory.retrieveCacheDataStore().saveNotifications(notifications)
    }

}