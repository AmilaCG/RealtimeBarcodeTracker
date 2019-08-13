#include "CV_Main.h"

using namespace std;
using namespace cv;

CV_Main::CV_Main()
        : m_camera_ready(false), m_image(nullptr), m_image_reader(nullptr),
          m_native_camera(nullptr) {
};

CV_Main::~CV_Main() {
    // clean up VM and callback handles
    JNIEnv *env;
    java_vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6);
    env->DeleteGlobalRef(calling_activity_obj);
    calling_activity_obj = nullptr;

    // ACameraCaptureSession_stopRepeating(m_capture_session);
    if (m_native_camera != nullptr) {
        delete m_native_camera;
        m_native_camera = nullptr;
    }

    // make sure we don't leak native windows
    if (m_native_window != nullptr) {
        ANativeWindow_release(m_native_window);
        m_native_window = nullptr;
    }

    if (m_image_reader != nullptr) {
        delete (m_image_reader);
        m_image_reader = nullptr;
    }
}

void CV_Main::OnCreate(JNIEnv *env, jobject caller_activity) {
    // Need to create an instance of the Java activity
    calling_activity_obj = env->NewGlobalRef(caller_activity);

    // Need to enter package and class to find Java class
    jclass handler_class = env->GetObjectClass(caller_activity);

    // Create function pointeACameraManager_getCameraCharacteristicsr to use for
    // on_loaded callbacks
    // on_callback = env->GetMethodID(handler_class, "JAVA_FUNCTION", "()V");
}

void CV_Main::OnPause() {}

void CV_Main::OnDestroy() {}

void CV_Main::SetNativeWindow(ANativeWindow *native_window) {
    // Save native window
    m_native_window = native_window;
}

void CV_Main::SetUpCamera() {

    m_native_camera = new Native_Camera(m_selected_camera_type);

    m_native_camera->MatchCaptureSizeRequest(&m_view,
                                             ANativeWindow_getWidth(m_native_window),
                                             ANativeWindow_getHeight(m_native_window));

    ASSERT(m_view.width && m_view.height, "Could not find supportable resolution");

    // Here we set the buffer to use RGBX_8888 as default might be; RGB_565
    ANativeWindow_setBuffersGeometry(m_native_window, m_view.height, m_view.width,
                                     WINDOW_FORMAT_RGBX_8888);

    m_image_reader = new Image_Reader(&m_view, AIMAGE_FORMAT_YUV_420_888);
    m_image_reader->SetPresentRotation(m_native_camera->GetOrientation());

    ANativeWindow *image_reader_window = m_image_reader->GetNativeWindow();

    m_camera_ready = m_native_camera->CreateCaptureSession(image_reader_window);
}

void CV_Main::CameraLoop() {
    bool buffer_printout = false;

    while (1) {
        if (m_camera_thread_stopped) { break; }
        if (!m_camera_ready || !m_image_reader) { continue; }
        m_image = m_image_reader->GetLatestImage();
        if (m_image == nullptr) { continue; }

        ANativeWindow_acquire(m_native_window);
        ANativeWindow_Buffer buffer;
        if (ANativeWindow_lock(m_native_window, &buffer, nullptr) < 0) {
            m_image_reader->DeleteImage(m_image);
            m_image = nullptr;
            continue;
        }

        if (false == buffer_printout) {
            buffer_printout = true;
            LOGI("/// H-W-S-F: %d, %d, %d, %d", buffer.height, buffer.width, buffer.stride,
                 buffer.format);
        }

        m_image_reader->DisplayImage(&buffer, m_image);

        display_mat = Mat(buffer.height, buffer.stride, CV_8UC4, buffer.bits);

        BarcodeDetect(display_mat);

        ANativeWindow_unlockAndPost(m_native_window);
        ANativeWindow_release(m_native_window);
    }
}

void CV_Main::BarcodeDetect(Mat &frame) {

    vector<Rect> faces;
    Mat frame_gray;

    cvtColor(frame, frame_gray, CV_RGBA2GRAY);

    //ellipse(frame, Point(100, 100), Size(10, 10), 0, 0, 360, CV_PURPLE, 4, 8, 0);

}