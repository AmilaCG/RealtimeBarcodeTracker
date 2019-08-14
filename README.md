# Realtime Barcode Tracker (Android)  
This is my first approach of getting a bounding box around a barcode in realtime.  
In this project, camera handling and image processing logic is implemented in the Android Java layer (in MainActivity). Used OpenCV SDK as an Android Studio module.  
Even though it is relatively easy to develop using this approach, performance is very low due to the data marshalling between Java and Native (C++) layer.
