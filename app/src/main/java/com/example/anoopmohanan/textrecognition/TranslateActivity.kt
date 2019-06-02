package com.example.anoopmohanan.textrecognition

import android.app.AlertDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.*
import kotlinx.android.synthetic.main.activity_translate.*
import android.view.inputmethod.InputMethodManager


class TranslateActivity : AppCompatActivity() {

    private var sourceLanguage = -1
    private var targetLanguage = -1
    private var areModelsDownloaded = false
    private var currentTranslator: FirebaseTranslator? = null
    private var models = mutableSetOf<FirebaseTranslateRemoteModel>()
    private var stringPassed = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)
        setUpPickers()
        progressBar.visibility = View.GONE
        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras == null) {
                stringPassed = ""
            } else {
                stringPassed = extras.getString("textToTranslate")
            }
        } else {
            stringPassed = savedInstanceState.getSerializable("textToTranslate") as String
        }
        sourceTextField.setText(stringPassed)
    }

    fun dismiss(view:View){

        finish()
    }
    fun translate(view: View){
        hideKeyboard()
        if (sourceTextField.text.isBlank()){
            showAlert("Please enter a text to translate")
            return
        }
        if (sourceLanguage == -1 || targetLanguage == -1){
            showAlert("Please choose languages")
            return
        }
        progressBar.visibility = View.VISIBLE
        currentTranslator = prepareATranslatorWith(sourceLanguage, targetLanguage)

    }

    private fun setUpPickers(){
        val data = arrayOf("None","English", "Swedish","German", "French")
        var fromPicker = fromPicker
        fromPicker.minValue = 0
        fromPicker.maxValue = data.size - 1
        fromPicker.displayedValues = data
        fromPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        fromPicker.setOnValueChangedListener{picker, oldvalue, newvalue ->

            sourceLanguage = getFirebaseLanguageFrom(data[newvalue])
        }

        var toPicker = toPicker
        toPicker.minValue = 0
        toPicker.maxValue = data.size - 1
        toPicker.displayedValues = data
        toPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
        toPicker.setOnValueChangedListener{picker, oldvalue, newvalue ->

            targetLanguage = getFirebaseLanguageFrom(data[newvalue])
        }
    }

    private fun prepareAllDownloadedModels(){
        val modelManager = FirebaseTranslateModelManager.getInstance()
        modelManager.getAvailableModels(FirebaseApp.getInstance())
                .addOnSuccessListener { models ->
                    // ...
                    this.models = models
                }
                .addOnFailureListener {
                    // Error.
                }

    }
    // Fetch a translator based on the language selected by the user.
    private fun prepareATranslatorWith(sourceLang: Int, destinationLang:Int ): FirebaseTranslator {

        // Create an English-German translator:
        val options = FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(destinationLang)
                .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        // Check for model
        translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    areModelsDownloaded = true
                    runOnUiThread {
                        // Your dialog code.
                        progressBar.visibility = View.GONE
                        translateTextNow()
                    }
                }
                .addOnFailureListener{
                    showAlert("Failed to download model try again")
                    areModelsDownloaded = false
                }
        return  translator
    }

    private fun getFirebaseLanguageFrom(userSelection: String): Int{

        when (userSelection){

            "English" -> return FirebaseTranslateLanguage.EN
            "German"  -> return FirebaseTranslateLanguage.DE
            "French"  -> return FirebaseTranslateLanguage.FR
            "Swedish" -> return FirebaseTranslateLanguage.SV
        }
        // Unknown
        return -1

    }

    private fun showAlert(message: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Error")
        dialog.setMessage(message)
        dialog.setPositiveButton(" OK ",
                { dialog, id -> dialog.dismiss() })
        dialog.show()

    }

    private fun translateTextNow(){
        currentTranslator!!.translate(sourceTextField.text.toString())
                .addOnSuccessListener { translatedText ->
                    runOnUiThread{
                        targetTextField.text = translatedText
                    }
                }
                .addOnFailureListener {
                    runOnUiThread{
                        showAlert("Failed to translate")
                    }
                }
    }

    private fun hideKeyboard() {
        // Check if no view has focus:
        val view = this.getCurrentFocus()
        if (view != null) {
            val inputManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(view!!.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}
