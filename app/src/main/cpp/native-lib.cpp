#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <android/bitmap.h>
#include "Bmp2Mat.h"
#include "FaceDetection.h"

cv::CascadeClassifier cascade;
JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    // 返回jni的版本
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_myapplication_FaceDetection_bmpFaceDetection(JNIEnv *env, jobject thiz,
                                                              jobject bmp_src, jobject bmp_dest,
                                                              jstring sModelPath) {
    const char *charModelPath;
    charModelPath = env->GetStringUTFChars(sModelPath, NULL);
    std::string stdModelName(charModelPath);

    // load model
    if(cascade.empty()){
        cascade.load(stdModelName);
    }

    std::vector<cv::Rect> faces;
    // TODO: implement bmpFaceDetection()
    cv::Mat src, dst;
    Bmp2Mat(env, bmp_src, src);
    faces = faceDetection(src, dst, cascade);
//    Mat2Bmp(env, bmp_dest, dst);


    jintArray result = env->NewIntArray(faces.size()*4);
    int** temp = new int*[faces.size()];
    if(faces.size() > 0){
        for(int i=0; i<faces.size(); i++){
            temp[i] = new int[4];
            temp[i][0] = faces[i].x;
            temp[i][1] = faces[i].y;
            temp[i][2] = faces[i].width;
            temp[i][3] = faces[i].height;
        }
        env -> SetIntArrayRegion(result, 0, 4, temp[0]);
    }

    return result;
}

//extern "C"
//JNIEXPORT jboolean JNICALL
//Java_com_example_myapplication_FaceDetection_InitFaceDetaction(JNIEnv *env, jobject instance, jstring sModelPath) {
//
//    // TODO
//    const char *charModelPath;
//    charModelPath = env->GetStringUTFChars(sModelPath, NULL);
//    std::string stdModelName(charModelPath);
//
//    // load model
//    if(cascade.empty()){
//        cascade.load(stdModelName);
//        return true;
//    }
//    return false;
//}