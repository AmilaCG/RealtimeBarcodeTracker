cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE  on)
set(OPENCV_SOURCE_DIR /home/amila/OpenCV/OpenCV-android-sdk)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

include_directories(${OPENCV_SOURCE_DIR}/sdk/native/jni/include)

add_library(native-tracker SHARED
        native-lib.cpp
        CV_Main.cpp
        Native_Camera.cpp
        Image_Reader.cpp)

add_library(opencv SHARED IMPORTED)
set_target_properties(opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)

find_library(log-lib log)

target_link_libraries(
        native-tracker
        ${log-lib}
        opencv
        camera2ndk
        android
        mediandk)