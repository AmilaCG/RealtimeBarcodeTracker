cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE  on)
set(OPENCV_SOURCE_DIR /home/amila/OpenCV/OpenCV-android-sdk)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

include_directories(${OPENCV_SOURCE_DIR}/sdk/native/jni/include)

# Export ANativeActivity_onCreate()
set(CMAKE_SHARED_LINKER_FLAGS
        "${CMAKE_SHARED_LINKER_FLAGS} -u ANativeActivity_onCreate")

add_library(camera_textureview SHARED
        ${CMAKE_CURRENT_SOURCE_DIR}/camera/android_main.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/camera/camera_manager.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/camera/camera_engine.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/camera/camera_listeners.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/common/utils/camera_utils.cpp)

target_include_directories(camera_textureview PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/common)


add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

add_library(opencv SHARED IMPORTED)
set_target_properties(opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libopencv_java3.so)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

target_link_libraries( # Specifies the target library.
        camera_textureview
        ${log-lib}
        opencv
        dl
        android
        m
        camera2ndk)

target_link_libraries( # Specifies the target library.
        native-lib
        ${log-lib}
        opencv)