    package com.example.anoopmohanan.textrecognition

    import android.Manifest
    import android.app.Activity
    import android.app.AlertDialog
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.content.res.Configuration
    import android.graphics.Bitmap
    import android.net.Uri
    import android.os.Bundle
    import android.os.Environment
    import android.provider.MediaStore
    import android.support.v4.content.FileProvider
    import android.support.v7.app.AppCompatActivity
    import android.util.Log
    import android.view.View
    import com.google.firebase.ml.vision.FirebaseVision
    import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
    import com.google.firebase.ml.vision.common.FirebaseVisionImage
    import com.google.firebase.ml.vision.common.FirebaseVisionPoint
    import com.google.firebase.ml.vision.face.FirebaseVisionFace
    import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
    import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
    import com.google.firebase.ml.vision.label.FirebaseVisionLabel
    import com.google.firebase.ml.vision.text.FirebaseVisionText
    import kotlinx.android.synthetic.main.activity_main.*
    import java.io.File
    import java.io.IOException
    import java.text.SimpleDateFormat
    import java.util.*


    class MainActivity : AppCompatActivity() {

        val REQUEST_IMAGE_CAPTURE = 1
        var mCurrentPhotoPath: String = ""

        var selectedImage: Bitmap? = null
        lateinit var photoURI:Uri
        enum class UserPermission{
            CAMERA,
            WRITE_DATA
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            checkAndRequestPermissionsFor(arrayListOf(UserPermission.CAMERA, UserPermission.WRITE_DATA))
        }

        override fun onResume() {
            super.onResume()
        }

        override fun onPause() {
            super.onPause()
        }
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {

                // Once the image is captured, get it from the saved location
                val f = File(mCurrentPhotoPath)
                val contentUri = Uri.fromFile(f)

                if (getBitmapFromUri(contentUri) != null){
                    selectedImage = getBitmapFromUri(contentUri)!!
                }
                snapShotView.setImageBitmap(selectedImage)
            }
        }


        fun takePicture(view: View){

            clearLabel()
            dispatchTakePictureIntent()
        }

        private fun dispatchTakePictureIntent() {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                var photoFile:File? = null

                try {
                    //TODO: Clean job to clear all the used images
                    photoFile = createImageFile()
                }catch (ex:IOException ){

                }

                if (photoFile != null) {
                    photoURI = FileProvider.getUriForFile(
                        this,
                        "com.example.anoopmohanan.textrecognition.letsgetrecognized",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }


        @Throws(IOException::class)
        private fun createImageFile(): File {
            // Create an image file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
            )

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath()
            return image
        }

        private fun getBitmapFromUri(filePath: Uri): Bitmap? {
            var bitmap:Bitmap? = null
            try{
                var tempBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, filePath)
                bitmap = updateImage(tempBitmap)
            }catch (ex: IOException){

            }
            return bitmap
        }

        private fun updateImage(bitmap: Bitmap): Bitmap{

            val isLandScape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            var scaledImageWidth = 0.0
            var scaledImageHeight = 0.0

            when (isLandScape){

                (true)->{
                    scaledImageHeight = snapShotView.height.toDouble()
                    scaledImageWidth = bitmap.width.toDouble() * scaledImageHeight / bitmap.height.toDouble()
                }
                (false)->{
                    scaledImageWidth = snapShotView.width.toDouble()
                    scaledImageHeight = bitmap.height.toDouble() * scaledImageWidth / bitmap.width.toDouble()
                }
            }
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap,scaledImageWidth.toInt(),scaledImageHeight.toInt(),true)

            return resizedBitmap
        }

        /***
         *      _______           _
         *     |__   __|         | |
         *        | |  ___ __  __| |_
         *        | | / _ \\ \/ /| __|
         *        | ||  __/ >  < | |_
         *        |_| \___|/_/\_\ \__|
         *
         *
         */
        fun recognizePicture(view: View){
            detectText()
        }

        private fun processTextRecognitionData(texts: FirebaseVisionText) {

            var finalText:String =""
            val pieces = texts.blocks
            if (pieces.size == 0) {

                return
            }
            pieces.forEach { block ->
                block.lines.forEach { line ->
                    line.elements.forEach { element ->

                        var text = element.text
                        finalText += text +","
                        Log.d("TEXTRECOG",text)
                    }
                }
            }
            showAlert(finalText)

        }
        private fun detectText(){

            val img = selectedImage?.let { it } ?: kotlin.run { return }
            val image = FirebaseVisionImage.fromBitmap(img!!)
            val detector = FirebaseVision.getInstance().visionTextDetector

            detector.detectInImage(image)
                .addOnSuccessListener { texts ->
                    processTextRecognitionData(texts)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }

        }

        /***
         *      _           _          _
         *     | |         | |        | |
         *     | |     __ _| |__   ___| |___
         *     | |    / _` | '_ \ / _ | / __|
         *     | |___| (_| | |_) |  __| \__ \
         *     |______\__,_|_.__/ \___|_|___/
         *
         *
         */
        fun decodeImage(view: View){

            decodeImage()
        }

        private fun decodeImage(){

            val img = selectedImage?.let { it } ?: kotlin.run { return }
            val image = FirebaseVisionImage.fromBitmap(img!!)
            val detector = FirebaseVision.getInstance().visionLabelDetector

            detector.detectInImage(image)
                .addOnSuccessListener {labels ->
                    processLabels(labels)

                }
                .addOnFailureListener{exception ->
                    print(exception.localizedMessage)
                }
        }


        private fun processLabels(labels: List<FirebaseVisionLabel>){


            val lbl = labels.firstOrNull()
            var msg = lbl?.label + "," + lbl?.confidence
            updateLabel(msg)
            for (label in labels) {
                val text = label.label
                val entityId = label.entityId
                val confidence = label.confidence

                Log.d("TEXTRECOG",text + entityId + confidence)

            }

        }

        private fun updateLabel(message: String){

            this.imageLabel.text = message
        }
        private fun clearLabel(){

            this.imageLabel.text = ""
        }

        /***
         *      ____                          _
         *     |  _ \                        | |
         *     | |_) | __ _ _ __ ___ ___   __| | ___
         *     |  _ < / _` | '__/ __/ _ \ / _` |/ _ \
         *     | |_) | (_| | | | (_| (_) | (_| |  __/
         *     |____/ \__,_|_|  \___\___/ \__,_|\___|
         *
         *
         */
        fun decodeBarcode(view: View){

            decodeBarcode()
        }

        private fun decodeBarcode(){


    //        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
    //            .setBarcodeFormats(
    //                FirebaseVisionBarcode.FORMAT_QR_CODE,
    //                FirebaseVisionBarcode.FORMAT_AZTEC
    //            )
    //            .build()
    //        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
            val img = selectedImage?.let { it } ?: kotlin.run { return }
            val image = FirebaseVisionImage.fromBitmap(img!!)
            val detector = FirebaseVision.getInstance().visionBarcodeDetector

            detector.detectInImage(image)
                .addOnSuccessListener {barcodes ->
                    processBarcodes(barcodes)

                }
                .addOnFailureListener{exception ->
                    print(exception.localizedMessage)
                }
        }

        private fun processBarcodes(barcodes: List<FirebaseVisionBarcode>){


            for (barcode in barcodes) {
                val bounds = barcode.boundingBox
                val corners = barcode.cornerPoints

                val rawValue = barcode.rawValue

                val displayValue = barcode.displayValue
                val msg = displayValue?.let{ it }?:"Undefined"
                updateLabel("Barcode:$msg")
                val valueType = barcode.valueType
                // See API reference for complete list of supported types
                when (valueType) {
                    FirebaseVisionBarcode.TYPE_WIFI -> {
                        val ssid = barcode.getWifi()!!.getSsid()
                        val password = barcode.getWifi()!!.getPassword()
                        val type = barcode.getWifi()!!.getEncryptionType()
                    }
                    FirebaseVisionBarcode.TYPE_URL -> {
                        val title = barcode.getUrl()!!.getTitle()
                        val url = barcode.getUrl()!!.getUrl()
                    }
                    FirebaseVisionBarcode.TYPE_UNKNOWN ->{

                        Log.d("TEXTRECOG","UNKNOWN BARCODE")
                    }
                }
            }

        }

        /***
         *      ______
         *     |  ____|
         *     | |__ __ _  ___ ___ ___
         *     |  __/ _` |/ __/ _ / __|
         *     | | | (_| | (_|  __\__ \
         *     |_|  \__,_|\___\___|___/
         *
         *
         */

        fun decodeFaces(view: View){

            decodeFaces()
        }

        private fun decodeFaces(){

            val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()
            val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

            //        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            //            .setBarcodeFormats(
            //                FirebaseVisionBarcode.FORMAT_QR_CODE,
            //                FirebaseVisionBarcode.FORMAT_AZTEC
            //            )
            //            .build()
            //        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)
            val img = selectedImage?.let { it } ?: kotlin.run { return }
            val image = FirebaseVisionImage.fromBitmap(img!!)


            detector.detectInImage(image)
                .addOnSuccessListener {faces ->
                    processFaces(faces)

                }
                .addOnFailureListener{exception ->
                    print(exception.localizedMessage)
                }
        }

        private fun processFaces(faces: List<FirebaseVisionFace>){

            for (face in faces) {
                val bounds = face.getBoundingBox()
                val rotY = face.getHeadEulerAngleY()  // Head is rotated to the right rotY degrees
                val rotZ = face.getHeadEulerAngleZ()  // Head is tilted sideways rotZ degrees

                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                // nose available):
                val leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR)
                if (leftEar != null) {
                    val leftEarPos = leftEar!!.getPosition()
                }

                var msg = "Face is"
                // If classification was enabled:
                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    val smileProb = face.getSmilingProbability()

                    if (smileProb > 50.0){

                        msg += " smiling"
                    }else{
                        msg += " numb"
                    }
                }
                if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                    val rightEyeOpenProb = face.getRightEyeOpenProbability()
                    if (rightEyeOpenProb > 50.0){

                        msg += "right eye open"
                    }
                }

                // If face tracking was enabled:
                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                    val id = face.getTrackingId()
                }
                updateLabel(msg)
            }

        }

        /***
         *      _____                    _         _
         *     |  __ \                  (_)       (_)
         *     | |__) ___ _ __ _ __ ___  _ ___ ___ _  ___  _ __  ___
         *     |  ___/ _ | '__| '_ ` _ \| / __/ __| |/ _ \| '_ \/ __|
         *     | |  |  __| |  | | | | | | \__ \__ | | (_) | | | \__ \
         *     |_|   \___|_|  |_| |_| |_|_|___|___|_|\___/|_| |_|___/
         *
         *
         */

        private fun checkAndRequestPermissionsFor(items: ArrayList<UserPermission>){

            var itemsRequirePermission = ArrayList<UserPermission>()
            for (item in items){

                if (!hasPermissionFor(item)){
                    itemsRequirePermission.add(item)
                }
            }
            if (!itemsRequirePermission.isEmpty()){
                requestPermissionFor(itemsRequirePermission)
            }

        }

        private fun hasPermissionFor(item: UserPermission): Boolean{

            var isPermitted = false
            when (item){

                UserPermission.CAMERA ->{

                    isPermitted = this.checkSelfPermission(Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED

                }
                UserPermission.WRITE_DATA ->{
                    isPermitted = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                }
            }
            return isPermitted
        }
        private fun requestPermissionFor(items: ArrayList<UserPermission>){

            var manisfestInfo = ArrayList<String>()
            for (item in items){

                manisfestInfo.add(getManisfestInfoFor(item))

            }
            val arrayOfPermissionItems = arrayOfNulls<String>(manisfestInfo.size)
            manisfestInfo.toArray(arrayOfPermissionItems)
            this.requestPermissions(arrayOfPermissionItems, 2)

        }

        private fun getManisfestInfoFor(item: UserPermission): String{

            var manifestString = ""
            when (item){

                UserPermission.CAMERA ->{

                    manifestString = Manifest.permission.CAMERA
                    //this.requestPermissions(arrayOf<String>(Manifest.permission.CAMERA), 1)

                }
                UserPermission.WRITE_DATA ->{
                    manifestString = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    //this.requestPermissions(arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE), 2)
                }
            }
            return manifestString
        }


        private fun showAlert(message: String) {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Recognized Text")
            dialog.setMessage(message)
            dialog.setPositiveButton(" OK ",
                { dialog, id -> dialog.dismiss() })
            dialog.show()

        }
    }

