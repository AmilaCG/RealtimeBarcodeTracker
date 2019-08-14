#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <thread>
#include "CV_Main.h"

static CV_Main app;

#ifdef __cplusplus
extern "C" {
#endif

jint JNI_OnLoad(JavaVM *vm, void *) {
    // We need to store a reference to the Java VM so that we can call back
    app.SetJavaVM(vm);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_amilaabeygunasekara_nativebarcodetracker_MainActivity_onCreateJNI(
        JNIEnv *env, jobject clazz, jobject activity, jobject j_asset_manager) {
    app.OnCreate(env, activity);
    app.SetAssetManager(AAssetManager_fromJava(env, j_asset_manager));
}

// Alot of stuff depends on the m_frame_buffer being loaded
// this is done in SetNativeWindow
JNIEXPORT void JNICALL
Java_com_amilaabeygunasekara_nativebarcodetracker_MainActivity_setSurface(JNIEnv *env,
                                                                          jclass clazz,
                                                                          jobject surface) {
    // obtain a native window from a Java surface
    app.SetNativeWindow(ANativeWindow_fromSurface(env, surface));

    // Set camera parameters up
    app.SetUpCamera();

    std::thread loopThread(&CV_Main::CameraLoop, &app);
    loopThread.detach();
}

// Destruct CV_Main
JNIEXPORT void JNICALL
Java_com_amilaabeygunasekara_nativebarcodetracker_MainActivity_releaseCVMain(JNIEnv *env,
                                                                             jclass clazz) {
    app.~CV_Main();
}

#ifdef __cplusplus
}
#endif
