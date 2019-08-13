# Realtime Barcode Tracker
The goal of this project is to track and decode multiple barcodes and show bounding boxes around them.

Currently this can detect a single barcode and display a bounding box around it in realtime.  

This uses Android NDK Camera2 API to access the camera from the native (C++) layer and then draw the preview on a SurfaceView using the NDK ANativeWindow.  
Using OpenCV to generate the bounding box with some image processing.  
This NDK solution will give much better fps rate rather than using OpenCV on the Java layer (with CameraBridgeViewBase.CvCameraViewListener2, can be found in "BarcodeTrackingJava" branch).
