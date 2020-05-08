package com.example.smartpark.Views

import android.app.Activity
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.smartpark.Data.Institutes
import com.example.smartpark.R
import com.example.smartpark.Utils.EventsUtil
import kotlinx.android.synthetic.main.activity_repetitive_event.*
import java.text.SimpleDateFormat
import java.util.*

class RepetitiveEvent : AppCompatActivity(), View.OnClickListener,
    TimePickerDialog.OnTimeSetListener {

    private var time = ""
    private val simpleHourFormat = SimpleDateFormat("HH:mm")
    private lateinit var eventsUtil: EventsUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repetitive_event)

        eventsUtil = EventsUtil(this)

        this.loadSpinnerInstitutes()
        this.loadSpinnerDaysOfWeek()
        this.setListeners()
    }

    // Load all values to put in the Spinner that show all institutes
    private fun loadSpinnerInstitutes() {
        spinnerInstRepEvent.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            Institutes.getInstituteNamesList())
    }

    // Put all days of week in the spinner
    private fun loadSpinnerDaysOfWeek() {

        val daysOfWeek = listOf<String>("Segunda-feira", "Terça-feira", "Quarta-feira",
            "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")

        spinnerDaysWeek.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item,
            daysOfWeek)
    }

    // Set all listeners
    private fun setListeners() {
        sendButtonRtvEvent.setOnClickListener(this)
        timePickerRtvEvent.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val id = view.id

        // Deal with the click of the button "Enviar"
        if (id == R.id.sendButtonRtvEvent) {
            val eventId = (System.currentTimeMillis() and 0xfffffff).toInt()

            // Verify if all the fields are filled
            if (!rtvEventTitle.text.isEmpty() && !time.isEmpty()) {
                eventsUtil.insertEventInDataBase(
                    eventId.toString(),
                    rtvEventTitle.text.toString(),
                    spinnerInstRepEvent.selectedItemPosition.toString(),
                    spinnerInstRepEvent.selectedItem.toString(),
                    spinnerDaysWeek.selectedItem.toString(),
                    time,
                    1)

                Toast.makeText(this, "Evento salvo", Toast.LENGTH_SHORT).show()
            } else {

                var textError = "Favor completar os campos: "

                if (rtvEventTitle.text.isEmpty()) {
                    textError += "título, "
                }
                if (time.isEmpty()) {
                    textError += "horário, "
                }

                textError = textError.substring(0, textError.length - 2)
                Toast.makeText(this, textError, Toast.LENGTH_LONG).show()
            }

        }
        // Deal with the click of the button "Selecione uma hora"
        else if (id == R.id.timePickerRtvEvent) {
            this.openTimePickerDialog()
        }
    }

    // Open the dialog with the calendar to pick a time
    private fun openTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE) + 1

        TimePickerDialog(this, this, hourOfDay, minute, true).show()
    }

    // Deal with the time that are selected
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day, hourOfDay, minute)
        time = simpleHourFormat.format(calendar.time)
        timePickerRtvEvent.text = time
    }
}
