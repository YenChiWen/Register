// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.
#include <string>
#include <vector>
#include <sstream>
#include <iostream>
#include <pthread.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <dirent.h>
// ncnn
#include "net.h"
#include "squeezencnn_jni.h"

using namespace cv;
using namespace std;

int DecryptedModelfile(const string& caffemodelname)
{
    const int complement[] =  { 1, -2, 4, -3, 2, -2, 1, -3, 2, -1, 3, -3, 2, -3, 1, -3 };
    int modvalue;
    FILE* fp = NULL;
    FILE* fr = NULL;

    fp = fopen(caffemodelname.c_str(), "rb+");
    if (fp == NULL) return -1;

    long len = 0;
    fseek(fp, 0, SEEK_END);
    len = ftell(fp);
    rewind(fp);

    fr = fopen((caffemodelname+".uncry").c_str(), "wb+");

    for (int i = 0; i < len; i++)
    {    //***Encrypt and Decrypt***//
        modvalue = i % 16;
        switch (modvalue)
        {
            case 0:
            {fputc(fgetc(fp) - complement[0], fr); break; }
            case 1:
            {fputc(fgetc(fp) - complement[1], fr); break; }
            case 2:
            {fputc(fgetc(fp) - complement[2], fr); break; }
            case 3:
            {fputc(fgetc(fp) - complement[3], fr); break; }
            case 4:
            {fputc(fgetc(fp) - complement[4], fr); break; }
            case 5:
            {fputc(fgetc(fp) - complement[5], fr); break; }
            case 6:
            {fputc(fgetc(fp) - complement[6], fr); break; }
            case 7:
            {fputc(fgetc(fp) - complement[7], fr); break; }
            case 8:
            {fputc(fgetc(fp) - complement[8], fr); break; }
            case 9:
            {fputc(fgetc(fp) - complement[9], fr); break; }
            case 10:
            {fputc(fgetc(fp) - complement[10], fr); break; }
            case 11:
            {fputc(fgetc(fp) - complement[11], fr); break; }
            case 12:
            {fputc(fgetc(fp) - complement[12], fr); break; }
            case 13:
            {fputc(fgetc(fp) - complement[13], fr); break; }
            case 14:
            {fputc(fgetc(fp) - complement[14], fr); break; }
            case 15:
            {fputc(fgetc(fp) - complement[15], fr); break; }
            default:
                break;
        }

    }

    fclose(fr);
    fclose(fp);
    return 0;
}

int DecryptedPrototxtfile(const string& protoname)
{
    const int complement[] = { 3, -1, 2, -2, 4, -1, 3, 3, -2, 1, 2, 2, 1, 1, 3, 2 };
    int modvalue;
    FILE* fp = NULL;
    FILE* fr = NULL;

    fp = fopen(protoname.c_str(), "rb+"); if (fp == NULL) return -1;

    long len = 0;
    fseek(fp, 0, SEEK_END);
    len = ftell(fp);
    rewind(fp);

    fr = fopen((protoname+".uncry").c_str(), "wb+");

    for (int i = 0; i < len; i++)
    {    //***Encrypt and Decrypt***//
        modvalue = i % 16;
        switch (modvalue)
        {
            case 0:
            {fputc(fgetc(fp) - complement[0], fr); break; }
            case 1:
            {fputc(fgetc(fp) - complement[1], fr); break; }
            case 2:
            {fputc(fgetc(fp) - complement[2], fr); break; }
            case 3:
            {fputc(fgetc(fp) - complement[3], fr); break; }
            case 4:
            {fputc(fgetc(fp) - complement[4], fr); break; }
            case 5:
            {fputc(fgetc(fp) - complement[5], fr); break; }
            case 6:
            {fputc(fgetc(fp) - complement[6], fr); break; }
            case 7:
            {fputc(fgetc(fp) - complement[7], fr); break; }
            case 8:
            {fputc(fgetc(fp) - complement[8], fr); break; }
            case 9:
            {fputc(fgetc(fp) - complement[9], fr); break; }
            case 10:
            {fputc(fgetc(fp) - complement[10], fr); break; }
            case 11:
            {fputc(fgetc(fp) - complement[11], fr); break; }
            case 12:
            {fputc(fgetc(fp) - complement[12], fr); break; }
            case 13:
            {fputc(fgetc(fp) - complement[13], fr); break; }
            case 14:
            {fputc(fgetc(fp) - complement[14], fr); break; }
            case 15:
            {fputc(fgetc(fp) - complement[15], fr); break; }
            default:
                break;
        }

    }

    fclose(fr);
    fclose(fp);
    return 0;
}

static ncnn::Net squeezenets;
static ncnn::Net squeezenetslandmark;
static vector<string> m_names;
static vector<vector<float>> m_pRegPoints;

JavaVM* g_JavaVM;
bool g_bAttatedT = false;
jclass g_jOpencvCls = NULL;
jobject g_TFObjCall = NULL;

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	g_JavaVM = vm;
	return JNI_VERSION_1_6;
}

JNIEnv* GetEnv()
{
	int status;
	JNIEnv* envnow = NULL;
	status = g_JavaVM->GetEnv((void **)&envnow, JNI_VERSION_1_6);

	if(status < JNI_OK)
	{
		status = g_JavaVM->AttachCurrentThread(&envnow, NULL);
		if(status < JNI_OK)
		{
			LOGE("jni AttachCurrentThread failed");
			return NULL;
		}
		g_bAttatedT = true;
	}

	//LOGE("return env=%p", envnow);
	return envnow;
}

void DetachCurrent()
{
	if(g_bAttatedT)
	{
		g_JavaVM->DetachCurrentThread();
		g_bAttatedT = false;
	}
}

Mat GetFaceMat(JNIEnv *env, jobject context, cv::Mat& img)
{
	Mat temImage, temResize;
	temImage = img;

	jint orig_w = (jint) temImage.cols;
	jint orig_h = (jint) temImage.rows;

	resize(temImage, temResize, Size(300, 300), 0, 0, INTER_LINEAR);

	jclass tf_cls = env->GetObjectClass(g_TFObjCall);//env->FindClass("com/viatech/viafacedetectlib/TensorFlowObjectDetectionAPIModel");
	jmethodID create_mid = env->GetStaticMethodID(tf_cls, "create", "(Landroid/content/res/AssetManager;)Lcom/viatech/viafacedetectlib/Classifier;");

	jclass  context_cls = env->GetObjectClass(context);
	jmethodID getAssets = env->GetMethodID(context_cls, "getAssets", "()Landroid/content/res/AssetManager;");

	jobject create_obj = env->CallStaticObjectMethod(tf_cls, create_mid, env->CallObjectMethod(context, getAssets));

//    if(env->ExceptionCheck())
//        return;

	jclass matclass = g_jOpencvCls;//env->FindClass("org/opencv/core/Mat");
	jmethodID jMatCons = env->GetMethodID(matclass, "<init>", "()V");
	jmethodID getPtrMethod = env->GetMethodID(matclass, "getNativeObjAddr", "()J");
	jobject jMat = env->NewObject(matclass, jMatCons);
	Mat &native_image = *(Mat *) env->CallLongMethod(jMat, getPtrMethod);
	native_image = temResize;

	jmethodID recognize_mid = env->GetMethodID(tf_cls, "recognizeImageFromSocket", "(Lorg/opencv/core/Mat;II)[Landroid/graphics/Rect;");
	jobjectArray rect_objs = (jobjectArray) env->CallObjectMethod(create_obj, recognize_mid,
	                                                              jMat, orig_w, orig_h);

	jsize len = env->GetArrayLength(rect_objs);

	cv::Mat faceMat;

	if(len>0) {
		jobject rect_obj = env->GetObjectArrayElement(rect_objs, 0);
		jclass cls_rect = env->GetObjectClass(rect_obj);

		jfieldID id_l = env->GetFieldID(cls_rect, "left", "I");
		jfieldID id_t = env->GetFieldID(cls_rect, "top", "I");
		jfieldID id_r = env->GetFieldID(cls_rect, "right", "I");
		jfieldID id_b = env->GetFieldID(cls_rect, "bottom", "I");

		jint left = env->GetIntField(rect_obj, id_l);
		jint top = env->GetIntField(rect_obj, id_t);
		jint right = env->GetIntField(rect_obj, id_r);
		jint bottom = env->GetIntField(rect_obj, id_b);

		Rect rect(left, top, right - left, bottom - top);
		faceMat = temImage(rect).clone();
		env->DeleteLocalRef(rect_obj);
		//LOGE("jni gang:[%d,%d %d,%d]", left, top, right, bottom);
	} else {
		LOGE("CAUTION: register faces failed, tf finds no faces!!!");
	}

	return faceMat;
}

/*void getFaceDetectInfo(cv::Mat& ImageMat)
{
	JNIEnv *env = GetEnv();
	GetFaceMat(env, context, ImageMat);
	DetachCurrent();
}*/

Mat AlignFace(Mat& faceonly, Point l_eye, Point r_eye, Point nose, Point l_mouth, Point r_mouth)
{
	//----------------align face----------------
	float radian = (float)atan(((r_eye.y - l_eye.y)*1.0 / (r_eye.x - l_eye.x)));
	float angle = radian*57.3f;//radian*180.0 / 3.14;
	Mat transMat = getRotationMatrix2D(nose, angle, 1);
	Mat dst;
	warpAffine(faceonly, dst, transMat, faceonly.size());
	return dst;
}

void NcnnFaceLandmarkMat(cv::Mat& addrImage, float* landmarkPts)
{
	ncnn::Mat in;
	{
		in = ncnn::Mat::from_pixels_resize(addrImage.data, ncnn::Mat::PIXEL_GRAY, addrImage.cols, addrImage.rows, 40, 40);
	}

	ncnn::Extractor ex = squeezenetslandmark.create_extractor();
	ex.set_num_threads(1);
	ex.input(0, in);

	ncnn::Mat out;
	ex.extract(20, out);

	for (int j=0; j<out.c; j++)
	{
		const float* prob = out.data + out.cstep * j;
		landmarkPts[j] = prob[0];
	}
}

float calCosine(vector<float> arrayA,vector<float> arrayB,int length)
{
	float sumarrayA=0,sumarrayB=0;
	float cosine=0;

	for(int i=0;i<length;i++){
		sumarrayA+=arrayA[i]*arrayA[i];
		sumarrayB+=arrayB[i]*arrayB[i];
		cosine+=arrayA[i]*arrayB[i];
	}
	sumarrayA=sqrt(sumarrayA);
	sumarrayB=sqrt(sumarrayB);
	if((sumarrayA-0<0.0001)||(sumarrayB-0<0.0001)){
		return 0;
	}
	cosine/=(sumarrayA*sumarrayB);
	return 1-cosine;
}

Mat ColorToGray(const Mat &img)
{
	Mat gray;
	LOGE("image channel is %d \n", img.channels());
	if (img.channels() == 3)
	{
		cvtColor(img, gray, CV_BGR2GRAY);
		//cvtColor(img, gray, CV_RGB2GRAY);
	}
	else if (img.channels() == 4)
	{
		cvtColor(img, gray, CV_BGRA2GRAY);
	}
	else
	{
		gray = img;
	}

	return gray;
}

std::vector<float> getfaceid(const cv::Mat& face)
{
	std::vector<float> feature_r;
	Mat gray_face = ColorToGray(face);
	float landmarkPts[10]={0};
	int w = gray_face.cols, h = gray_face.rows;
	NcnnFaceLandmarkMat(gray_face, landmarkPts);
	Point l_eye(landmarkPts[0]*w, landmarkPts[1]*h), r_eye(landmarkPts[2]*w, landmarkPts[3]*h);
	Point nose(landmarkPts[4]*w, landmarkPts[5]*h);
	Point l_mouth(landmarkPts[6]*w, landmarkPts[7]*h), r_mouth(landmarkPts[8]*w, landmarkPts[9]*h);

	Mat img_r = AlignFace(gray_face, l_eye, r_eye, nose, l_mouth, r_mouth);
	ncnn::Mat in_r = ncnn::Mat::from_pixels_resize(img_r.data, ncnn::Mat::PIXEL_GRAY, img_r.cols, img_r.rows, 128, 128);
	//cv::imwrite("/sdcard/dst.png", img_r);
	ncnn::Extractor ex = squeezenets.create_extractor();
	//exs[i].set_light_mode(true);
	ex.input("data", in_r);
	ncnn::Mat out_r;
	ex.extract("eltwise_fc1", out_r);
	feature_r.resize(out_r.c);

	for (int j=0; j<out_r.c; j++){
	    const float* prob = out_r.channel(j);
	    feature_r[j] = prob[0];
	}

    return feature_r;
//
//	string name="person";
//	float smallest_similiar = 0xffff;
//
//	for (uint32_t i = 0; i < m_pRegPoints.size(); i++)
//	{
//	    float similar = calCosine(m_pRegPoints[i], feature_r, 256);
//
//	    //LOGE("similar=%f", similar);
//	    if (similar < 0.4)
//	    {
//	        if(similar < smallest_similiar)
//	        {
//	            smallest_similiar = similar;
//	            name = m_names[i];
//	        }
//	    }
//	}
//
//	memcpy(facename, name.c_str(), strlen(name.c_str()));
}

void via_add_face(Mat& face, const char* name)
{
	Mat gray_face = ColorToGray(face);
	float landmarkPts[10]={0};
	int w = gray_face.cols, h = gray_face.rows;
	NcnnFaceLandmarkMat(gray_face, landmarkPts);
	Point l_eye(landmarkPts[0]*w, landmarkPts[1]*h), r_eye(landmarkPts[2]*w, landmarkPts[3]*h);
	Point nose(landmarkPts[4]*w, landmarkPts[5]*h);
	Point l_mouth(landmarkPts[6]*w, landmarkPts[7]*h), r_mouth(landmarkPts[8]*w, landmarkPts[9]*h);
	Mat img_l = AlignFace(gray_face, l_eye, r_eye, nose, l_mouth, r_mouth);

	//cv::imwrite("/sdcard/src.png", img_l);

	std::vector<float> feature_l;
	ncnn::Mat in_l = ncnn::Mat::from_pixels_resize(img_l.data, ncnn::Mat::PIXEL_GRAY, img_l.cols, img_l.rows, 128, 128);
	ncnn::Extractor ex = squeezenets.create_extractor();
	//exs[i].set_light_mode(true);
	ex.input("data", in_l);
	ncnn::Mat out_l;
	ex.extract("eltwise_fc1", out_l);

	feature_l.resize(out_l.c);
	for (int j = 0; j < out_l.c; j++) {
		const float *prob = out_l.channel(j);
		feature_l[j] = prob[0];
	}

	m_pRegPoints.emplace_back(feature_l);
	m_names.emplace_back((string)name);
}

#define FACE_NAME_LEN 100

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

JNIEXPORT jboolean JNICALL NCNN_METHOD(NCNNInit)(JNIEnv* env, jobject thiz, jstring path)
{
	const char* str = env->GetStringUTFChars(path,0);
	std::string model_path = str;

	string param_file = model_path + "ncnn_id.param";
	string model_file = model_path + "ncnn_id.bin";

	DecryptedModelfile(model_file);
	DecryptedPrototxtfile(param_file);

	if(squeezenets.load_param((param_file+".uncry").c_str()))
	{
		LOGE("load_param data failed");
		return JNI_FALSE;
	}

	if(squeezenets.load_model((model_file+".uncry").c_str()))
	{
		LOGE("load_model data failed");
		return JNI_FALSE;
	}

	remove((param_file+".uncry").c_str());
	remove((model_file+".uncry").c_str());

	param_file = model_path + "ncnn_landmark.param";
	model_file = model_path + "ncnn_landmark.bin";

	DecryptedModelfile(model_file);
	DecryptedPrototxtfile(param_file);

	if(squeezenetslandmark.load_param((param_file+".uncry").c_str()))
	{
		LOGE("load_param data failed");
		return JNI_FALSE;
	}

	if(squeezenetslandmark.load_model((model_file+".uncry").c_str()))
	{
		LOGE("load_model data failed");
		return JNI_FALSE;
	}

	remove((param_file+".uncry").c_str());
	remove((model_file+".uncry").c_str());

	return JNI_TRUE;
}

JNIEXPORT void JNICALL NCNN_METHOD(NCNNExit)(JNIEnv *env, jobject obj)
{
}

JNIEXPORT jfloatArray JNICALL NCNN_METHOD(GetFaceID)(JNIEnv *env, jobject obj, jlong addrImage)
{
	Mat& src_face = *(cv::Mat*)addrImage;
	Mat gray_face = ColorToGray(src_face);

    std::vector<float> feature_vector = getfaceid(src_face);
    jfloatArray result = env->NewFloatArray(feature_vector.size());
    for(int i=0;i<feature_vector.size();i++) {
        env->SetFloatArrayRegion(result,0,feature_vector.size(),(jfloat*)feature_vector.data());
    }


//	env->SetByteArrayRegion(byteJavaArray, 0, FACE_NAME_LEN, (jbyte*)result);
	return result;
}

JNIEXPORT void JNICALL NCNN_METHOD(AddFaceID)(JNIEnv *env, jobject obj, jobject context, jlong facemat, jstring facename)
{
	cv::Mat& mImage = *(cv::Mat*)facemat;
	const char* namestr = env->GetStringUTFChars(facename,0);
	std::string face_name = namestr;
	//cv::Mat face = GetFaceMat(env, context, mImage);
	via_add_face(mImage, face_name.c_str());
	env->ReleaseStringUTFChars(facename, namestr);
	LOGE("jni AddFaceID!");
}

#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus