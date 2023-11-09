#include <windows.h>
#include "jni.h"
#include "jvmti.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

JavaVM* jvm;
JNIEnv* jniEnv;
jvmtiEnv* jvmti;


struct Callback {
    const unsigned char* array;
    jint length;
    int success;
};

struct TransformCallback {
    jclass clazz;
    struct Callback* callback;
    struct TransformCallback* next;
};

static struct TransformCallback* callback_list = NULL;

void JNICALL classFileLoadHook(jvmtiEnv* jvmti_env, JNIEnv* env,
    jclass class_being_redefined, jobject loader,
    const char* name, jobject protection_domain,
    jint class_data_len, const unsigned char* class_data,
    jint* new_class_data_len, unsigned char** new_class_data) {

    *new_class_data = NULL;

    if (class_being_redefined) {
        struct TransformCallback* current = callback_list;
        struct TransformCallback* previous = NULL;

        while (current != NULL) {
            if (!(*env)->IsSameObject(env, current->clazz, class_being_redefined)) {
                previous = current;
                current = current->next;
                continue;
            }

            if (previous == NULL) {
                callback_list = current->next;
            }
            else {
                previous->next = current->next;
            }

            current->callback->array = class_data;
            current->callback->length = class_data_len;
            current->callback->success = 1;

            free(current);
            break;
        }
    }
}

void* allocate(jlong size) {
    void* resultBuffer = malloc(size);
    return resultBuffer;
}

JNIEXPORT jbyteArray JNICALL GetClassBytes(JNIEnv* env, jclass _, jclass clazz) {
    struct Callback* retransform_callback = (struct Callback*)allocate(sizeof(struct Callback));
    retransform_callback->success = 0;

    struct TransformCallback* new_node = (struct TransformCallback*)allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;

    jclass* classes = (jclass*)allocate(sizeof(jclass));
    classes[0] = clazz;

    jint err = (*jvmti)->RetransformClasses((jvmtiEnv*)jvmti, 1, classes);

    if (err > 0) {
        printf("jvmti error while getting class bytes: %d\n", err);
        return NULL;
    }

    jbyteArray output = (*env)->NewByteArray(env, retransform_callback->length);
    (*env)->SetByteArrayRegion(env, output, 0, retransform_callback->length, (jbyte*)retransform_callback->array);

    free(classes);
    return output;
}

JNIEXPORT jint JNICALL RedefineClass(JNIEnv* env, jclass _, jclass clazz, jbyteArray classBytes) {
    jbyte* classByteArray = (*env)->GetByteArrayElements(env, classBytes, NULL);
    struct Callback* retransform_callback = (struct Callback*)allocate(sizeof(struct Callback));
    retransform_callback->success = 0;
    struct TransformCallback* new_node = (struct TransformCallback*)allocate(sizeof(struct TransformCallback));
    new_node->clazz = clazz;
    new_node->callback = retransform_callback;
    new_node->next = callback_list;
    callback_list = new_node;
    jvmtiClassDefinition* definitions = (jvmtiClassDefinition*)allocate(sizeof(jvmtiClassDefinition));
    definitions->klass = clazz;
    definitions->class_byte_count = (*env)->GetArrayLength(env, classBytes);
    definitions->class_bytes = (unsigned char*)classByteArray;
    jint error = (jint)(*jvmti)->RedefineClasses((jvmtiEnv*)jvmti, 1, definitions);
    (*env)->ReleaseByteArrayElements(env, classBytes, classByteArray, 0);
    free(definitions);
    return error;
}

jclass DefineClass(JNIEnv* env, jobject obj, jobject classLoader, jbyteArray bytes)
{
    jclass clClass = (*env)->FindClass(env, "java/lang/ClassLoader");
    jmethodID defineClass = (*env)->GetMethodID(env, clClass, "defineClass", "([BII)Ljava/lang/Class;");
    jobject classDefined = (*env)->CallObjectMethod(env, classLoader, defineClass, bytes, 0, (*env)->GetArrayLength(env, bytes));
    return (jclass)classDefined;
}

const char* getLoaderJarPath() {
    char userProfile[MAX_PATH];
    char modsFolderPath[MAX_PATH];
    DWORD result = GetEnvironmentVariableA("USERPROFILE", userProfile, MAX_PATH);
    if (result != 0 && result < MAX_PATH) {
        sprintf_s(modsFolderPath, MAX_PATH, "%s\\.vanillamodifier\\Loader.jar", userProfile);
        return modsFolderPath;
    }
    else {
        return NULL;
    }
}

DWORD WINAPI Inject(LPVOID parm) {
    HMODULE jvmHandle = GetModuleHandle(L"jvm.dll");
    if (!jvmHandle) return 0;
    typedef jint(JNICALL* fnJNI_GetCreatedJavaVMs)(JavaVM**, jsize, jsize*);
    fnJNI_GetCreatedJavaVMs JNI_GetCreatedJavaVMs = (fnJNI_GetCreatedJavaVMs)GetProcAddress(jvmHandle, "JNI_GetCreatedJavaVMs");
    if (!JNI_GetCreatedJavaVMs) return 0;
    if (JNI_GetCreatedJavaVMs(&jvm, 1, NULL) != JNI_OK || (*jvm)->AttachCurrentThread(jvm, (void**)&jniEnv, NULL) != JNI_OK) return 0;
    (*jvm)->GetEnv(jvm, (void**)&jvmti, JVMTI_VERSION_1_2);
    if (!jvmti) return 0;
    jclass threadClass = (*jniEnv)->FindClass(jniEnv, "java/lang/Thread");
    jmethodID getAllStackTraces = (*jniEnv)->GetStaticMethodID(jniEnv, threadClass, "getAllStackTraces", "()Ljava/util/Map;");
    if (!getAllStackTraces) return 0;
    jobjectArray threads = (jobjectArray)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallStaticObjectMethod(jniEnv, threadClass, getAllStackTraces), (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->FindClass(jniEnv, "java/util/Map"), "keySet", "()Ljava/util/Set;")), (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->FindClass(jniEnv, "java/util/Set"), "toArray", "()[Ljava/lang/Object;"));
    if (!threads) return 0;
    jsize arrlength = (*jniEnv)->GetArrayLength(jniEnv, threads);
    jobject clientThread = NULL;
    for (int i = 0; i < arrlength; i++) {
        jobject thread = (*jniEnv)->GetObjectArrayElement(jniEnv, threads, i);
        if (thread == NULL) continue;
        jclass threadClass = (*jniEnv)->GetObjectClass(jniEnv, thread);
        jstring name = (*jniEnv)->CallObjectMethod(jniEnv, thread, (*jniEnv)->GetMethodID(jniEnv, threadClass, "getName", "()Ljava/lang/String;"));
        const char* str = (*jniEnv)->GetStringUTFChars(jniEnv, name, FALSE);
        if (!strcmp(str, "Client thread")) {
            clientThread = thread;
            (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
            break;
        }
        (*jniEnv)->ReleaseStringUTFChars(jniEnv, name, str);
    }
    if (!clientThread) return 0;
    jclass urlClassLoader = (*jniEnv)->FindClass(jniEnv, "java/net/URLClassLoader");
    jmethodID findClass = (*jniEnv)->GetMethodID(jniEnv, urlClassLoader, "findClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    jmethodID addURL = (*jniEnv)->GetMethodID(jniEnv, urlClassLoader, "addURL", "(Ljava/net/URL;)V");
    jclass fileClass = (*jniEnv)->FindClass(jniEnv, "java/io/File");
    jmethodID init = (*jniEnv)->GetMethodID(jniEnv, fileClass, "<init>", "(Ljava/lang/String;)V");
    jstring filePath = (*jniEnv)->NewStringUTF(jniEnv, getLoaderJarPath());
    jobject file = (*jniEnv)->NewObject(jniEnv, fileClass, init, filePath);
    jmethodID toURI = (*jniEnv)->GetMethodID(jniEnv, fileClass, "toURI", "()Ljava/net/URI;");
    jobject uri = (*jniEnv)->CallObjectMethod(jniEnv, file, toURI);
    jclass URIClass = (*jniEnv)->FindClass(jniEnv, "java/net/URI");
    jmethodID toURL = (*jniEnv)->GetMethodID(jniEnv, URIClass, "toURL", "()Ljava/net/URL;");
    jobject url = (*jniEnv)->CallObjectMethod(jniEnv, uri, toURL);
    (*jniEnv)->CallVoidMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), addURL, url);
    jstring entryClass = (*jniEnv)->NewStringUTF(jniEnv, "org/vanillamodifier/loader/Loader");
    jclass clazz = (jclass)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), findClass, entryClass);

    jvmtiCapabilities capabilities = { 0 };
    memset(&capabilities, 0, sizeof(jvmtiCapabilities));

    capabilities.can_get_bytecodes = 1;
    capabilities.can_redefine_classes = 1;
    capabilities.can_redefine_any_class = 1;
    capabilities.can_generate_all_class_hook_events = 1;
    capabilities.can_retransform_classes = 1;
    capabilities.can_retransform_any_class = 1;

    (*jvmti)->AddCapabilities((jvmtiEnv*)jvmti, &capabilities);

    jvmtiEventCallbacks callbacks = { 0 };
    memset(&callbacks, 0, sizeof(jvmtiEventCallbacks));

    callbacks.ClassFileLoadHook = &classFileLoadHook;

    (*jvmti)->SetEventCallbacks((jvmtiEnv*)jvmti, &callbacks, sizeof(jvmtiEventCallbacks));
    (*jvmti)->SetEventNotificationMode((jvmtiEnv*)jvmti, JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, NULL);

    jstring wrapper = (*jniEnv)->NewStringUTF(jniEnv, "org/vanillamodifier/loader/NativeWrapper");
    jclass wrapperClass = (jclass)(*jniEnv)->CallObjectMethod(jniEnv, (*jniEnv)->CallObjectMethod(jniEnv, clientThread, (*jniEnv)->GetMethodID(jniEnv, (*jniEnv)->GetObjectClass(jniEnv, clientThread), "getContextClassLoader", "()Ljava/lang/ClassLoader;")), findClass, wrapper);

    JNINativeMethod methods[] =
    {
        {"getClassBytes", "(Ljava/lang/Class;)[B", (void*)&GetClassBytes},
        {"redefineClass", "(Ljava/lang/Class;[B)I", (void*)&RedefineClass},
        {"defineClass", "(Ljava/lang/ClassLoader;[B)Ljava/lang/Class;", (void*)&DefineClass}
    };

    (*jniEnv)->RegisterNatives(jniEnv, wrapperClass, methods, 3);

    jmethodID loaderid = NULL;
    loaderid = (*jniEnv)->GetMethodID(jniEnv,clazz, "<init>", "()V");
    jobject LoadClent = (*jniEnv)->NewObject(jniEnv, clazz, loaderid);
    (*jvm)->DetachCurrentThread(jvm);
    return 0;
}

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    switch (fdwReason)
    {
    case DLL_PROCESS_ATTACH:
    {
        CreateThread(NULL, 4096, &Inject, NULL, 0, NULL);
        break;
    }
    }
    return TRUE;
}