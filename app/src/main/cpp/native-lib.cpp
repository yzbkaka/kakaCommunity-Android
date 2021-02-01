#include <jni.h>
#include <strings.h>
#include <fcntl.h>
#include <dlfcn.h>

#include <string>

#include <android/log.h>

#include "fake_dlfcn.h"
#include "include/inlineHook.h"

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
 * 获得当前线程的JNIEnv
 * @return 当前线程的JNIEnv
 */
JNIEnv *getJNIEnv() {
    JNIEnv *_jniEnv = NULL;
    if(my_java_vm == NULL) return NULL;
    int status;
    status = (*my_java_vm).GetEnv((void **) &_jniEnv, JNI_VERSION_1_6);
    if (status == JNI_EDETACHED || _jniEnv == NULL) {
        status = (*my_java_vm).AttachCurrentThread(&_jniEnv, NULL);
        if (status < 0) {
            _jniEnv = NULL;
        }
    }
    return _jniEnv;
}

/**
 * 通过fake获得open方法的地址
 *
 * @param env JNI环境变量
 * @return open函数的原型地址
 */
open_fun getOpenFun() {
    void *handle;
    if (sizeof(void*) == sizeof(uint64_t)) {
        LOGD("64 bit mode.");
        handle = fake_dlopen("/system/lib64/libjavacore.so", RTLD_NOW);
    } else {
        LOGD("32 bit mode.");
        handle = fake_dlopen("/system/lib/libjavacore.so", RTLD_NOW);
    }
    if (handle == 0) {
        LOGD("fake_dlopen failed!");
        return 0;
    }
    open_fun fun = (open_fun) fake_dlsym(handle, "open");
    if(fun == 0) {
        LOGD("fake_dlsym failed!");
        return 0;
    }
    return fun;
}

/**
 * 回调Java层打出堆栈信息
 */
void printStackTrace() {
    JNIEnv *env;
    jclass cls;
    jmethodID mid;
    env = getJNIEnv();
    if(env == NULL)  LOGD("env = NULL");
    else{
        LOGD("开始打堆栈");
        //cls = env->GetObjectClass(my_jobject);
        cls = env->FindClass("com/example/kakacommunity/utils/NativeMethodHelper");
        mid = env->GetStaticMethodID(cls, "printStackTrace", "()V");
        env->CallStaticVoidMethod(cls,mid);

    }
}

/**
 * hook open后的方法
 *
 * @param pathname 欲打开的文件路径字符串
 * @param flags 使用的旗标
 * @param mode 模式
 * @return 文件句柄fd
 */
int hookOpen(const char *pathname, int flags, mode_t mode) {
    LOGD("hook open start!");
    const open_fun fun = (open_fun)proto_addr;
    int fd = fun(pathname,flags,mode);
    printStackTrace();
    return fd;
}

/**
 * 获取当前JNI环境，赋值给全局变量
 *
 * @param env JNI环境
 * @param thiz 方法所在的对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_kakacommunity_utils_NativeMethodHelper_getEnv(JNIEnv *env, jobject thiz) {
    LOGD("get ENV");
    env->GetJavaVM(&my_java_vm);
    my_jobject = env->NewGlobalRef(thiz);
}

/**
 * 开始进行hook操作
 *
 * @param env JNI环境
 * @param thiz 方法所在的对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_kakacommunity_utils_NativeMethodHelper_startHook(JNIEnv *env, jobject thiz) {
    LOGD("start hook");
    open_fun fun = getOpenFun();
    if(fun == 0) {
        LOGD("open() not found in libjavacore.so!");
        return;
    }
    if (registerInlineHook((uint32_t) (fun), (uint32_t) (hookOpen), (uint32_t **) (&proto_addr)) != ELE7EN_OK) {
        LOGE("register error");
    }
    if(inlineHook((uint32_t)(fun)) != ELE7EN_OK) {
        LOGE("inlineHook error");
    }
}

/**
 * 停止hook，恢复原型函数
 *
 * @param env JNI环境
 * @param thiz 方法所在的对象
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_kakacommunity_utils_NativeMethodHelper_stopHook(JNIEnv *env, jobject thiz) {
    open_fun fun = getOpenFun();
    inlineUnHook((uint32_t) (fun));
}
