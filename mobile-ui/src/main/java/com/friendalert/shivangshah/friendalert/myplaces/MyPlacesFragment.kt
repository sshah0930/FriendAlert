package com.friendalert.shivangshah.friendalert.myplaces

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.Nullable
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.friendalert.shivangshah.friendalert.DataLoadingListener
import com.friendalert.shivangshah.friendalert.R
import com.friendalert.shivangshah.model.myplaces.request.MyPlaceRequestModel
import com.friendalert.shivangshah.model.myplaces.response.MyPlaceModel
import com.friendalert.shivangshah.presentation.myplaces.MyPlacesContract
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


/**
 * Created by shivangshah on 11/15/17.
 */
class MyPlacesFragment : Fragment(), MyPlacesContract.View, OnMapReadyCallback, GoogleMap.OnMarkerClickListener, MyPlaceActionListener, MarkerViewClickedListener {

    lateinit var googleMap : GoogleMap
    var clickedMarker: Marker? = null
    lateinit var searchEditText : EditText

    lateinit var mRelativeLayout: RelativeLayout

    val strokeWidth = 5f
    val fillColor = 0x26107F93

    var PLACE_AUTOCOMPLETE_REQUEST = 4000

    var hashMapMyPlace: HashMap<Int, MyPlaceViewModel> = HashMap()

    var dataLoadingListener: DataLoadingListener? = null

    private var retryButton: Button? = null

    @Inject lateinit var myPlacePresenter : MyPlacesContract.Presenter

    override fun setPresenter(presenter: MyPlacesContract.Presenter) {
        myPlacePresenter = presenter;
    }

    fun instantiate(@Nullable arguments: Bundle): MyPlacesFragment {
        val myPlaceFragment = MyPlacesFragment()
        myPlaceFragment.setArguments(arguments)
        return myPlaceFragment
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        dataLoadingListener = activity as DataLoadingListener
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var view = inflater!!.inflate(R.layout.fragment_myplaces, container, false)

        searchEditText = view.findViewById(R.id.searchEditText)
        retryButton = view.findViewById<Button>(R.id.retryButton)

        mRelativeLayout = view.findViewById<RelativeLayout>(R.id.layoutRL)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchEditText.setOnClickListener { v ->
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(activity);
                activity!!.startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST);
            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            }
        }

        retryButton!!.setOnClickListener {

            v: View? ->  myPlacePresenter.retrieveMyPlaces()

        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(activity, data);
                Log.i(TAG, "Place: " + place.getName());

                val nickname = place.name
                val address = place.address
                val city = "city"
                val state = "state"
                val latitude = place.latLng.latitude.toString()
                val longitude = place.latLng.longitude.toString()

                // TODO : Show create new myplace popup

                val myPlaceViewModelData = MyPlaceRequestModel(0,"", nickname.toString(), address.toString(), city, state, latitude, longitude, 1, "5")

                val actionMyPlaceFragment = ActionMyPlaceDialogFragment().newInstance(myPlaceViewModelData, true, this)
                actionMyPlaceFragment.show(activity!!.supportFragmentManager, "")

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(activity, data);
                // TODO: Handle the error.
                Log.i(TAG, status.statusMessage);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0!!
        googleMap.setOnMarkerClickListener(this)

        myPlacePresenter.start()
    }

    override fun onMarkerClick(p0: Marker?): Boolean {

        clickedMarker = p0!!

        var myPlace: MyPlaceModel? = null

        for(entry in hashMapMyPlace.entries){
            if(entry.value.marker == clickedMarker){
                myPlace = entry.value.myPlace
            }
        }

        val displayText = if(myPlace!!.nickname == null || myPlace!!.nickname == "") myPlace!!.address else myPlace!!.nickname

        showSnackBar(displayText!!, true)

        return true
    }

    fun markerSnackBarClicked(){

        var myPlace: MyPlaceModel? = null

        for(entry in hashMapMyPlace.entries){
            if(entry.value.marker == clickedMarker){
                myPlace = entry.value.myPlace
            }
        }

        val myPlaceRequestModel = MyPlaceRequestModel(myPlace!!.base_camp_id,myPlace.fk_user_id, myPlace.nickname, myPlace.address, myPlace.city, myPlace.state, myPlace.latitude, myPlace.longitude, myPlace.active, myPlace.radius)

        val actionMyPlaceFragment = ActionMyPlaceDialogFragment().newInstance(myPlaceRequestModel, false, this)
        actionMyPlaceFragment.show(activity!!.supportFragmentManager, "")
    }

    override fun showNoMyPlacesAvailable(message: String) {

        val builder = AlertDialog.Builder(context!!);
        builder.setTitle("No MyPlaces Found")
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton("Okay", { dialog, _ -> dialog.cancel() })

        val alert = builder.create()
        alert.show();

    }


    override fun showMyPlaces(myPlaces: List<MyPlaceModel>) {

        for(myPlaceObj in myPlaces){

            val latlng = LatLng(myPlaceObj.latitude.toDouble(), myPlaceObj.longitude.toDouble())

//            var img = BitmapFactory.decodeResource(resources, R.drawable.pin)
//            var bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);

            val markerOptions = MarkerOptions().position(latlng).title(myPlaceObj.nickname).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            val marker = googleMap.addMarker(markerOptions)

            val circleOptions = CircleOptions().center(latlng).radius(myPlaceObj.radius.toDouble() * 1609.34).fillColor(fillColor).strokeColor(Color.parseColor("#107F93")).strokeWidth(strokeWidth);
            val circle = googleMap.addCircle(circleOptions)

            hashMapMyPlace.put(myPlaceObj.base_camp_id, MyPlaceViewModel(marker, circle, myPlaceObj))

        }

        updateCamera()

    }

    override fun addMyPlace(myPlace: MyPlaceModel) {

        val latlng = LatLng(myPlace.latitude.toDouble(), myPlace.longitude.toDouble())

//        var img = BitmapFactory.decodeResource(resources, R.drawable.pin)
//        var bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(img);

        val markerOptions = MarkerOptions().position(latlng).title(myPlace.nickname).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
        val marker = googleMap.addMarker(markerOptions)

        val circleOptions = CircleOptions().center(latlng).radius(myPlace.radius.toDouble() * 1609.34).fillColor(fillColor).strokeColor(Color.parseColor("#107F93")).strokeWidth(strokeWidth);
        val circle = googleMap.addCircle(circleOptions)

        hashMapMyPlace.put(myPlace.base_camp_id, MyPlaceViewModel(marker, circle, myPlace))

        updateCamera()

    }

    override fun deleteMyPlace(myPlace: MyPlaceModel) {

        val marker = hashMapMyPlace[myPlace.base_camp_id]!!.marker
        marker.remove()

        val circle = hashMapMyPlace[myPlace.base_camp_id]!!.circle
        circle.remove()

        hashMapMyPlace.remove(myPlace.base_camp_id)

        updateCamera()
    }

    override fun editMyPlace(myPlace: MyPlaceModel) {

        // update viewmodel object with updated myplace object (updated nickname/radius)
        hashMapMyPlace[myPlace.base_camp_id]!!.myPlace = myPlace

        // remove old initials_circle from google map
        hashMapMyPlace[myPlace.base_camp_id]!!.circle.remove()

        // create new initials_circle with updated radius
        var circleOptions = CircleOptions().center(LatLng(myPlace.latitude.toDouble(), myPlace.longitude.toDouble())).radius(myPlace.radius.toDouble() * 1609.34).fillColor(fillColor).strokeColor(Color.parseColor("#107F93")).strokeWidth(strokeWidth);

        // add new initials_circle to google map
        val circle = googleMap.addCircle(circleOptions)

        // update viewmodel object with new initials_circle
        hashMapMyPlace[myPlace.base_camp_id]!!.circle = circle

        updateCamera()

    }

    private fun updateCamera(){

        if(hashMapMyPlace.values.count() > 0){

            val builder = LatLngBounds.Builder()
            val values = hashMapMyPlace.values
            for(myPlaceValue in values){

                val targetNorthEast = SphericalUtil.computeOffset(myPlaceValue.circle.center, myPlaceValue.circle.radius * Math.sqrt(2.0), 45.0);
                val targetSouthWest = SphericalUtil.computeOffset(myPlaceValue.circle.center, myPlaceValue.circle.radius * Math.sqrt(2.0), 225.0);

                builder.include(targetNorthEast)
                builder.include(targetSouthWest)
            }

            val bounds = builder.build();

            val padding = 100;
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.animateCamera(cu);

        }
    }

    override fun createMyPlacePressed(myPlace: MyPlaceRequestModel) {
        myPlacePresenter.createMyPlace(myPlace)
    }

    override fun editMyPlacePressed(myPlace: MyPlaceRequestModel) {
        myPlacePresenter.editMyPlace(myPlace)
    }

    override fun deleteMyPlacePressed(myPlace: MyPlaceRequestModel) {
        myPlacePresenter.deleteMyPlace(myPlace.base_camp_id)
    }

    override fun showProgress() {

        dataLoadingListener!!.dataLoadingStart()

    }

    override fun hideProgress() {

        dataLoadingListener!!.dataLoadingStop()

    }

    override fun showSuccess() {

        retryButton!!.visibility = View.GONE

    }

    override fun showFailure(firstTime: Boolean, errorMessage: String) {

        showSnackBar(errorMessage, false)

        if(firstTime){
            retryButton!!.visibility = View.VISIBLE
        }

    }


    override fun onStop() {

        myPlacePresenter.stop()
        super.onStop()
    }

    fun showSnackBar(message: String, action: Boolean){

        val snackbar = Snackbar.make(mRelativeLayout,message, Snackbar.LENGTH_LONG);
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.primaryColor, null));
        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ResourcesCompat.getColor(resources, R.color.whiteColor, null));

        if(action){
            snackbar.setAction("View", { markerSnackBarClicked() })
            snackbar.setActionTextColor(ResourcesCompat.getColor(resources, R.color.whiteColor, null))
        }

        snackbar.show();

    }
}