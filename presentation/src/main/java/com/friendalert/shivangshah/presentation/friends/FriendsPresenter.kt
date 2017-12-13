package com.friendalert.shivangshah.presentation.friends

import com.friendalert.shivangshah.domain.friends.GetFriends
import com.friendalert.shivangshah.domain.myplaces.MyPlaces
import com.friendalert.shivangshah.model.friends.response.FriendsResponseModel
import com.friendalert.shivangshah.presentation.CustomResponseCodes
import io.reactivex.observers.DisposableSingleObserver
import javax.inject.Inject

/**
 * Created by shivangshah on 12/12/17.
 */
class FriendsPresenter @Inject constructor(val friendsView: FriendsContract.View,
                                           val getFriends: GetFriends)
    : FriendsContract.Presenter{

    init {
        friendsView.setPresenter(this)
    }

    override fun start() {


    }

    override fun getFriends() {
        getFriends.execute(GetFriendsSubscriber())
    }

    inner class GetFriendsSubscriber: DisposableSingleObserver<FriendsResponseModel>() {

        override fun onSuccess(t: FriendsResponseModel) {

            if(t.customCode == CustomResponseCodes.getSuccess)
            {

            }else{

            }

        }

        override fun onError(exception: Throwable) {


        }

    }

    override fun stop() {
        getFriends.dispose()
    }


}