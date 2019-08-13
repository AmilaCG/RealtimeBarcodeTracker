# RealtimeBarcodeTracking
The goal of this project is to track and decode multiple barcodes and show bounding boxes around them.

Currently this can detect a single barcode and display a bounding box around it in realtime.  

This uses Android NDK Camera2 API to access the camera from the native layer and then draw the preview on a SurfaceView using the NDK ANativeWindow.  
This solution will give much better fps rate rather than using opencv on the Java layer (with CameraBridgeViewBase.CvCameraViewListener2).
