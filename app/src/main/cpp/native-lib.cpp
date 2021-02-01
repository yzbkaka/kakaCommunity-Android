#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG    "JNILOG"
#define LOGD(...)   __android_log_print((int)ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print((int)ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * 原始地址
 */
static void *proto_addr;

/**
 * open函数原型
 */
typedef int (*open_fun)(const char *pathname, int flags, mode_t mode);

/**
 * 全局环境变量
 */
JavaVM *my_java_vm = NULL;
jobject my_jobject = NULL;


/**
 * 获取当前JNI环境，赋值给全局变量
 *
 * @param env JNI环境
 * @param thiz 方法所在的对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_inlinehooktestproject_NativeMethodHelper_getEnv(JNIEnv *env, jobject thiz) {
    LOGD("get ENV");
    env->GetJavaVM(&my_java_vm);
    my_jobject = env->NewGlobalRef(thiz);
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_kakacommunity_utils_NativeMethodHelper_startHook(JNIEnv *env, jobject thiz) {
    // TODO: implement startHook()
}


extern "C"
JNIEXPORT void JNICALL
Java_com_example_kakacommunity_utils_NativeMethodHelper_stopHook(JNIEnv *env, jobject thiz) {
    // TODO: implement stopHook()
}