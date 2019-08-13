cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE  on)
set(OPENCV_SOURCE_DIR /home/amila/OpenCV/OpenCV-android-sdk)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

include_directories(${OPENCV_SOURCE_DIR}/sdk/native/jni/include)

add_library(native-lib SHARED
        native-lib.cpp)

add_library(opencv SHARED IMPORTED)
set_target_properties(opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)

find_library(log-lib log)

target_link_libraries(
        native-lib
        ${log-lib}
        opencv)