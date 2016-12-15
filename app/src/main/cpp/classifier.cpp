#include <jni.h>

extern "C"
JNIEXPORT jint JNICALL
Java_org_elins_aktvtas_human_HumanActivityClassifier_classify(
        JNIEnv *env, jobject humanActivityClassifier, jobject sensorDataSequence) {
    return 1;
}
