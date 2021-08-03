package com.codepath.bookself;

import android.graphics.Bitmap;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;

public class BarcodeScanner {

    private BarcodeScanner(){
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13)
                .build();
    }

    public void processImage(Bitmap image) {

    }
}
