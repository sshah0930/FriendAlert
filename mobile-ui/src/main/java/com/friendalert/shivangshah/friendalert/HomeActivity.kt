package com.friendalert.shivangshah.friendalert

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.FrameLayout
import com.friendalert.shivangshah.friendalert.broadcast.BroadcastFragment
import com.friendalert.shivangshah.friendalert.friends.FriendsFragment
import com.friendalert.shivangshah.friendalert.myplaces.MyPlacesFragment
import com.friendalert.shivangshah.friendalert.notifications.NotificationsFragment
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

class HomeActivity : AppCompatActivity(), HasSupportFragmentInjector, BottomNavigationView.OnNavigationItemSelectedListener {

    @Inject lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    private val PLAY_SERVICES_REQUEST = 1000
    private val REQUEST_CHECK_SETTINGS = 2000
    private val READ_CONTACTS_REQUEST = 3000
    private val PLACE_AUTOCOMPLETE_REQUEST = 4000

    private var bottomNavigationView: BottomNavigationView? = null
    private var containerLayout : FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_home)

        bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        containerLayout = findViewById<FrameLayout>(R.id.containerLayout);

        bottomNavigationView?.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_broadcast -> switchToBroadcastFragment()
            R.id.menu_myplace -> switchToMyPlacesFragment()
            R.id.menu_friends -> switchToFriendsFragment()
            R.id.menu_notifications -> switchToNotificationsFragment()
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_CHECK_SETTINGS) {
            val fragment = supportFragmentManager.findFragmentByTag("BroadcastFragment")
            fragment.onActivityResult(requestCode, resultCode, data)
        }
      else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST){
            val fragment = supportFragmentManager.findFragmentByTag("MyPlacesFragment")
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        if(requestCode == PLAY_SERVICES_REQUEST){
            val fragment : Fragment = supportFragmentManager.findFragmentByTag("BroadcastFragment")
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }else if(requestCode == READ_CONTACTS_REQUEST){
            val fragment : Fragment = supportFragmentManager.findFragmentByTag("FriendsFragment")
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    fun switchToBroadcastFragment() {
        val manager = supportFragmentManager
        manager.beginTransaction().add(R.id.containerLayout, BroadcastFragment().instantiate(Bundle()), "BroadcastFragment").commit()
    }

    fun switchToNotificationsFragment() {
        val manager = supportFragmentManager
        manager.beginTransaction().replace(R.id.containerLayout, NotificationsFragment().instantiate(Bundle()), "NotificationsFragment").commit()
    }

    fun switchToMyPlacesFragment() {
        val manager = supportFragmentManager
        manager.beginTransaction().replace(R.id.containerLayout, MyPlacesFragment().instantiate(Bundle()), "MyPlacesFragment").commit()
    }

    fun switchToFriendsFragment() {
        val manager = supportFragmentManager
        manager.beginTransaction().replace(R.id.containerLayout, FriendsFragment().instantiate(Bundle()), "FriendsFragment").commit()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }
}
