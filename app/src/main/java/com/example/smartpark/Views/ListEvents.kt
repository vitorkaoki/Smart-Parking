package com.example.smartpark.Views

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.smartpark.Data.DatabaseHandler
import com.example.smartpark.Adapters.EventListAdapter
import com.example.smartpark.Models.Event
import com.example.smartpark.R
import com.example.smartpark.R.*
import com.example.smartpark.Utils.EventsUtil
import kotlinx.android.synthetic.main.access_event_dialog.*
import kotlinx.android.synthetic.main.activity_list_events.*
import kotlinx.android.synthetic.main.delete_event_dialog.*

class ListEvents : AppCompatActivity(), View.OnClickListener {

    private var repetitive = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_list_events)

        setListeners()
    }

    override fun onStart() {
        super.onStart()
        showAllEvents(repetitive)
    }

    private fun setListeners() {
        btnToggleSingle.setOnClickListener(this)
        btnToggleRepetitive.setOnClickListener(this)
        btnToggleSingle.isClickable = false
        btnToggleSingle.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        btnToggleRepetitive.isClickable = true
    }

    override fun onClick(view: View) {
        val id = view.id
        val typedValue = TypedValue()

        // Show the single events
        if (id == R.id.btnToggleSingle) {
            repetitive = 0
            showAllEvents(repetitive)

            theme.resolveAttribute(attr.selectableItemBackground, typedValue, true)

            btnToggleSingle.background = ContextCompat.getDrawable(this, typedValue.resourceId)
            btnToggleSingle.setTextColor(ContextCompat.getColor(this, color.thirdColor))
            btnToggleRepetitive.background = ContextCompat.getDrawable(this, drawable.btn_list_event_rpt_bottom_left)
            btnToggleRepetitive.setTextColor(Color.parseColor("#FFFFFF"))
            titleListSingle.visibility = View.VISIBLE
            titleListRepetitive.visibility = View.GONE
            btnToggleSingle.isClickable = false
            btnToggleRepetitive.isClickable = true
            btnToggleSingle.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            btnToggleRepetitive.paintFlags -= Paint.UNDERLINE_TEXT_FLAG

        }
        // Show the repetitive events
        else if (id == R.id.btnToggleRepetitive) {
            repetitive = 1
            showAllEvents(repetitive)

            theme.resolveAttribute(attr.selectableItemBackground, typedValue, true)

            btnToggleSingle.background = ContextCompat.getDrawable(this, drawable.btn_list_event_sng_bottom_right)
            btnToggleSingle.setTextColor(Color.parseColor("#FFFFFF"))
            btnToggleRepetitive.background = ContextCompat.getDrawable(this, typedValue.resourceId)
            btnToggleRepetitive.setTextColor(ContextCompat.getColor(this, color.thirdColor))
            titleListSingle.visibility = View.GONE
            titleListRepetitive.visibility = View.VISIBLE
            btnToggleSingle.isClickable = true
            btnToggleRepetitive.isClickable = false
            btnToggleSingle.paintFlags -= Paint.UNDERLINE_TEXT_FLAG
            btnToggleRepetitive.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        }
    }

    // Get all the events stored and show in a list
    private fun showAllEvents(repetitive: Int) {
        // Get all the events stored
        val dbHelper = DatabaseHandler(this)
        var events = dbHelper.readAllEvents(repetitive)

        // Inflate the listView with all the events stored
        val eventAdapter = EventListAdapter(
            this,
            layout.event_row,
            events
        )
        eventsList.adapter = eventAdapter

        eventsList.setOnItemClickListener { parent, view, position, id ->
            val event = eventAdapter.getItem(position)
            if (event != null) {

                // If it is a single event, open a dialog to confirm access. If it is not, just go
                // to the near institutes parking lots activity
                if (repetitive == 0) {
                    this.sngEventItemClick(event)
                } else {
                    this.goToNearInstPLActivity(event)
                }
            }
        }

        // Open a dialog when long click on event item, to confirm its delete or not
        eventsList.setOnItemLongClickListener { parent, view, position, id ->
            val event = eventAdapter.getItem(position)
            if (event != null) {
                this.eventItemLongClick(event.getEventId())
            }
            true
        }

    }

    // Go to the activity that will show the number of parking lots per near institutes
    private fun goToNearInstPLActivity(event: Event) {
        val intent = Intent(this, NearInstitutesParkingLots::class.java)
        intent.putExtra("instituteId", event.getInstituteId())
        startActivity(intent)
    }

    // Deal with the click in an event in list view
    private fun sngEventItemClick(event: Event) {

        // Inflate the dialog with the layout created for the dialog box
        val dialogView = LayoutInflater.from(this).inflate(layout.access_event_dialog, null)

        // Build the alert dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        //Show the dialog
        val alertDialog = alertDialogBuilder.show()

        // Deal with confirmAccess button to access the event
        alertDialog.confirmAccess.setOnClickListener {

            this.goToNearInstPLActivity(event)

            // If the event is accessed it is deleted
            val eventsUtil = EventsUtil(this)
            eventsUtil.deleteEventFromDatabase(event.getEventId())

            // Dismiss the alert dialog
            alertDialog.dismiss()
        }

        // Deal with dismissAccess button to not access the notification
        alertDialog.dismissAccess.setOnClickListener {
            // Dismiss the alert dialog
            alertDialog.dismiss()
        }
    }

    // Delete an event selected by the user from the list of events
    private fun eventItemLongClick(eventId: String) {

        // Inflate the dialog with the layout created for the dialog box
        val dialogView = LayoutInflater.from(this).inflate(layout.delete_event_dialog, null)

        // Build the alert dialog
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        //Show the dialog
        val alertDialog = alertDialogBuilder.show()

        // Deal with confirmDelete button to delete the event
        alertDialog.confirmDelete.setOnClickListener {
            val eventsUtil = EventsUtil(this)
            val result = eventsUtil.deleteEventFromDatabase(eventId)

            if(result == -1) {
                Toast.makeText(this, "Erro ao deletar o evento", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Evento deletado com sucesso", Toast.LENGTH_SHORT).show()
            }

            // Dismiss the alert dialog
            alertDialog.dismiss()

            showAllEvents(repetitive)
        }

        // Deal with dismissDelete button to not delete the notification
        alertDialog.dismissDelete.setOnClickListener {
            // Dismiss the alert dialog
            alertDialog.dismiss()
        }
    }
}


