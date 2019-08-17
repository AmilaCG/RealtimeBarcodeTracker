# Realtime Barcode Tracker (Android)
The goal of this project is to track and decode multiple barcodes and show bounding boxes around them.

Currently this can detect a single barcode and display a bounding box around it in realtime.  

This uses Android NDK Camera2 API to access the camera from the native (C++) layer and then draw the preview on a SurfaceView using the NDK ANativeWindow.  
Using OpenCV to generate the bounding box with some image processing.  
This NDK solution will give much better fps rate rather than using OpenCV on the Java layer (with CameraBridgeViewBase.CvCameraViewListener2, can be found in "BarcodeTrackingJava" branch).

## Prerequisites

- Android API 24 (Nougat) or greater
- [OpenCV-Android-SDK](https://sourceforge.net/projects/opencvlibrary/files/opencv-android/)

## How to setup

- Clone or download the repo and open "NativeBarcodeTracker" project using Android Studio.
- Change the OpenCV Android SDK library path in [CMakeLists.txt](NativeBarcodeTracker/app/src/main/cpp/CMakeLists.txt#L4) file to your downloaded `OpenCV-android-sdk` path.

## How to run on platforms other than arm64-v8a

To make compiling faster and to minimize the project size I only have it load and run ARM 64-bit (`arm64-v8a`). To add a different abi architecture, lets take ARM 32-bit (`armeabi-v7a`) as an example:

- In the [build.gradle](NativeBarcodeTracker/app/build.gradle#L16#L19) file add `, "-DANDROID_ABI=armeabi-v7a"` in arguments and `, armeabi-v7a` in abi-filters. (You can also replace it with `arm64-v8a`)
- Go into the downloaded OpenCV Android SDK and copy `OpenCV-android-sdk/sdk/native/libs/armeabi-v7a/libopencv_java3.so` file into [jniLibs](NativeBarcodeTracker/app/src/main/jniLibs) folder by creating a directory named `armeabi-v7a`.
