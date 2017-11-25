package com.friendalert.shivangshah.friendalert.injection.module

import android.app.Application
import android.content.Context
import com.friendalert.shivangshah.cache.notifications.NotificationsCacheImpl
import com.friendalert.shivangshah.cache.PreferencesHelper
import com.friendalert.shivangshah.cache.db.DbOpenHelper
import com.friendalert.shivangshah.cache.notifications.NotificationEntityMapper
import com.friendalert.shivangshah.cache.user.UserCacheImpl
import com.friendalert.shivangshah.cache.user.UserResponseModelEntityMapper
import com.friendalert.shivangshah.data.notifications.NotificationDataRepository
import com.friendalert.shivangshah.data.JobExecutor
import com.friendalert.shivangshah.data.broadcast.BroadcastDataRepository
import com.friendalert.shivangshah.data.broadcast.BroadcastMapper
import com.friendalert.shivangshah.data.broadcast.CreateBroadcastResponseEntityMapper
import com.friendalert.shivangshah.data.broadcast.repository.BroadcastRemote
import com.friendalert.shivangshah.data.broadcast.source.BroadcastDataStoreFactory
import com.friendalert.shivangshah.data.myplaces.MyPlaceDataRepository
import com.friendalert.shivangshah.data.myplaces.MyPlaceMapper
import com.friendalert.shivangshah.data.myplaces.MyPlaceResponseEntityMapper
import com.friendalert.shivangshah.data.myplaces.MyPlacesMapper
import com.friendalert.shivangshah.data.myplaces.repository.MyPlaceRemote
import com.friendalert.shivangshah.data.myplaces.source.MyPlaceDataStoreFactory
import com.friendalert.shivangshah.data.notifications.NotificationMapper
import com.friendalert.shivangshah.data.notifications.repository.NotificationCache
import com.friendalert.shivangshah.data.notifications.repository.NotificationRemote
import com.friendalert.shivangshah.data.notifications.source.NotificationDataStoreFactory
import com.friendalert.shivangshah.data.user.UserDataRepository
import com.friendalert.shivangshah.data.user.UserMapper
import com.friendalert.shivangshah.data.user.UserResponseMapper
import com.friendalert.shivangshah.data.user.repository.UserCache
import com.friendalert.shivangshah.data.user.repository.UserRemote
import com.friendalert.shivangshah.data.user.source.UserDataStoreFactory
import com.friendalert.shivangshah.domain.PostExecutionThread
import com.friendalert.shivangshah.domain.ThreadExecutor
import com.friendalert.shivangshah.domain.broadcast.Broadcast
import com.friendalert.shivangshah.domain.broadcast.BroadcastRepository
import com.friendalert.shivangshah.domain.myplaces.MyPlaceRepository
import com.friendalert.shivangshah.domain.notifications.NotificationRepository
import com.friendalert.shivangshah.domain.user.UserRepository
import com.friendalert.shivangshah.friendalert.BuildConfig
import com.friendalert.shivangshah.friendalert.UiThread
import com.friendalert.shivangshah.friendalert.injection.component.HomeActivitySubComponent
import com.friendalert.shivangshah.friendalert.injection.component.LoginActivitySubComponent
import com.friendalert.shivangshah.friendalert.injection.component.SplashActivitySubComponent
import dagger.Module
import dagger.Provides
import com.friendalert.shivangshah.friendalert.injection.scopes.PerApplication
import com.friendalert.shivangshah.presentation.myplaces.MyPlacesPresentationModel
import com.friendalert.shivangshah.remote.broadcast.BroadcastRemoteImpl
import com.friendalert.shivangshah.remote.broadcast.BroadcastResponseModelEntityMapper
import com.friendalert.shivangshah.remote.broadcast.BroadcastService
import com.friendalert.shivangshah.remote.broadcast.BroadcastServiceFactory
import com.friendalert.shivangshah.remote.myplaces.*
import com.friendalert.shivangshah.remote.notifications.NotificationRemoteImpl
import com.friendalert.shivangshah.remote.notifications.NotificationService
import com.friendalert.shivangshah.remote.notifications.NotificationServiceFactory
import com.friendalert.shivangshah.remote.user.UserRemoteImpl
import com.friendalert.shivangshah.remote.user.UserRequestModelEntityMapper
import com.friendalert.shivangshah.remote.user.UserService
import com.friendalert.shivangshah.remote.user.UserServiceFactory


/**
 * We provide retrofit, okhttp, persistence db, shared pref etc
 * We have to add our subcomponents to AppModule so our dagger graph will understand that
 */
@Module(subcomponents = arrayOf(HomeActivitySubComponent::class,
                                LoginActivitySubComponent::class,
                                SplashActivitySubComponent::class))
open class ApplicationModule {

    @Provides
    @PerApplication
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @PerApplication
    internal fun providePreferencesHelper(context: Context): PreferencesHelper {
        return PreferencesHelper(context)
    }

    @Provides
    @PerApplication
    internal fun provideThreadExecutor(jobExecutor: JobExecutor): ThreadExecutor {
        return jobExecutor
    }

    @Provides
    @PerApplication
    internal fun providePostExecutionThread(uiThread: UiThread): PostExecutionThread {
        return uiThread
    }

    // User Dependencies
    @Provides
    @PerApplication
    internal fun provideUserRepository(factory: UserDataStoreFactory, userMapper: UserMapper, responseMapper: UserResponseMapper): UserRepository {
        return UserDataRepository(factory, userMapper, responseMapper)
    }

    @Provides
    @PerApplication
    internal fun provideUserCache(  helper: PreferencesHelper,
                                            mapper: com.friendalert.shivangshah.cache.user.UserMapper,
                                            responseMapper: UserResponseModelEntityMapper): UserCache {
        return UserCacheImpl(helper, mapper, responseMapper)
    }

    @Provides
    @PerApplication
    internal fun provideUserRemote(service: UserService,
                                   responseModelEntityMapper : com.friendalert.shivangshah.remote.user.UserResponseModelEntityMapper,
                                   requestModelEntityMapper: UserRequestModelEntityMapper): UserRemote {
        return UserRemoteImpl(service, responseModelEntityMapper,requestModelEntityMapper)
    }

    @Provides
    @PerApplication
    internal fun provideUserService(): UserService {
        return UserServiceFactory.makeUserService(BuildConfig.DEBUG)
    }


    // Notifications Dependencies ------------------------------------------------------------------
    @Provides
    @PerApplication
    internal fun provideNotificationsRepository(factory: NotificationDataStoreFactory,
                                                mapper: NotificationMapper): NotificationRepository {
        return NotificationDataRepository(factory, mapper)
    }

    @Provides
    @PerApplication
    internal fun provideNotificationsCache(factory: DbOpenHelper,
                                           entityMapper: NotificationEntityMapper,
                                           mapper: com.friendalert.shivangshah.cache.notifications.NotificationMapper,
                                           helper: PreferencesHelper): NotificationCache {
        return NotificationsCacheImpl(factory, entityMapper, mapper, helper)
    }

    @Provides
    @PerApplication
    internal fun provideNotificationsRemote(service: NotificationService,
                                            factory: com.friendalert.shivangshah.remote.notifications.NotificationEntityMapper): NotificationRemote {
        return NotificationRemoteImpl(service, factory)
    }

    @Provides
    @PerApplication
    internal fun provideNotificationsService(): NotificationService {
        return NotificationServiceFactory.makeNotificationService(BuildConfig.DEBUG)
    }

    // MyPlaces Dependencies -----------------------------------------------------------------------
    @Provides
    @PerApplication
    internal fun provideMyPlacesRepository(factory: MyPlaceDataStoreFactory,
                                           mapper: MyPlacesMapper,
                                           myPlaceMapper: MyPlaceMapper,
                                           myPlaceResponseEntityMapper: MyPlaceResponseEntityMapper): MyPlaceRepository {
        return MyPlaceDataRepository(factory, mapper,myPlaceMapper,myPlaceResponseEntityMapper)
    }

//    @Provides
//    @PerApplication
//    internal fun provideMyPlacesCache(factory: DbOpenHelper,
//                                      entityMapper: MyPlacesEntityMapper,
//                                      mapper: com.friendalert.shivangshah.cache.db.mapper.MyPlacesMapper,
//                                      helper: PreferencesHelper): MyPlaceCache {
//        return MyPlaceCacheImpl(factory, entityMapper, mapper, helper)
//    }

    @Provides
    @PerApplication
    internal fun provideMyPlacesRemote(service: MyPlaceService,
                                       myPlacesEntityMapper: com.friendalert.shivangshah.remote.myplaces.MyPlacesEntityMapper,
                                       myPlaceEntityMapper: MyPlaceEntityMapper,
                                       myPlaceResponseModelMapper: MyPlaceResponseModelMapper): MyPlaceRemote {
        return MyPlaceRemoteImpl(service, myPlacesEntityMapper, myPlaceEntityMapper, myPlaceResponseModelMapper)
    }

    @Provides
    @PerApplication
    internal fun provideMyPlacesService(): MyPlaceService {
        return MyPlaceServiceFactory.makeMyPlaceService(BuildConfig.DEBUG)
    }

    @Provides
    @PerApplication
    internal fun provideMyPlacesPresentationModel(): MyPlacesPresentationModel{
        return MyPlacesPresentationModel()
    }

    // Broadcast Dependencies ----------------------------------------------------------------------
    @Provides
    @PerApplication
    internal fun provideBroadcastRepository(factory: BroadcastDataStoreFactory,
                                            mapper: BroadcastMapper,
                                            createBroadcastResponseEntityMapper: CreateBroadcastResponseEntityMapper): BroadcastRepository {
        return BroadcastDataRepository(factory, mapper,createBroadcastResponseEntityMapper)
    }

    @Provides
    @PerApplication
    internal fun provideBroadcastRemote(service: BroadcastService,
                                       broadcastEntityMapper: com.friendalert.shivangshah.remote.broadcast.BroadcastEntityMapper,
                                       broadcastResponseModelMapper: BroadcastResponseModelEntityMapper): BroadcastRemote {
        return BroadcastRemoteImpl(service, broadcastEntityMapper, broadcastResponseModelMapper)
    }

    @Provides
    @PerApplication
    internal fun provideBroadcastService(): BroadcastService {
        return BroadcastServiceFactory.makeBroadcastService(BuildConfig.DEBUG)
    }
}
