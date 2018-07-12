
## Label Detection

#####To create a FirebaseVisionImage object from a Bitmap object:

```
FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

```

#####To create a FirebaseVisionImage object from a media.Image

```
FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);
```
#####To create a FirebaseVisionImage object from a ByteBuffer or a byte array

```
FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
        .setWidth(1280)
        .setHeight(720)
        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
        .setRotation(rotation)
        .build();
```

```
FirebaseVisionImage image = FirebaseVisionImage.fromByteBuffer(buffer, metadata);
// Or: FirebaseVisionImage image = FirebaseVisionImage.fromByteArray(byteArray, metadata);
```
##### To create a FirebaseVisionImage object from a file, pass the app context and file URI to FirebaseVisionImage.fromFilePath():

```
FirebaseVisionImage image;
try {
    image = FirebaseVisionImage.fromFilePath(context, uri);
} catch (IOException e) {
    e.printStackTrace();
}
```

##Barcode Detection

`ML Kit` can automatically recognize and parse data from all types of common barcode, you dont need to specify the format, your app can respond intelligently when a user scans a barcode.

```
* Reads most standard formats.
* Automatic format detection
* Extracts structured data
```

Structured data stored using one of the supported 2D formats are automatically parsed. Supported information types include

``` 
URLs 
contact information
calendar events 
email addresses
phone numbers
SMS message prompts 
ISBNs 
WiFi connection information 
geographic location 
AAMVA-standard driver information 
```


To recognize barcodes in an image, create a `FirebaseVisionImage `object from either a `Bitmap`, `media.Image`, `ByteBuffer`, `byte array`, or a `file` on the device. Then, pass the `FirebaseVisionImage` object to the `FirebaseVisionBarcodeDetector`'s `detectInImage` method.

## Face Detection

To detect faces in an image, create a FirebaseVisionImage object from either a Bitmap, media.Image, ByteBuffer, byte array, or a file on the device. Then, pass the FirebaseVisionImage object to the FirebaseVisionFaceDetector's detectInImage method.