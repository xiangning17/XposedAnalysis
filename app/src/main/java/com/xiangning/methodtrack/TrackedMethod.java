package com.xiangning.methodtrack;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class TrackedMethod implements Parcelable {

    /**
     * 包名
     */
    private String mPackageName;
    /**
     * 完整类名
     */
    private String mClassName;
    /**
     * 方法名，为空表示类的构造方法
     */
    private String mMethodName;
    /**
     * 方法参数类型，为空则所有同名方法都被track，无参方法需传入长度为0的数组
     */
    private String[] mMethodParamTypes;

    public TrackedMethod() {}

    public TrackedMethod(String packageName, String className, String methodName) {
        mPackageName = packageName;
        mClassName = className;
        mMethodName = methodName;
    }

    public TrackedMethod(String packageName, String className, String methodName, String[] methodParamTypes) {
        mPackageName = packageName;
        mClassName = className;
        mMethodName = methodName;
        mMethodParamTypes = methodParamTypes;
    }

    protected TrackedMethod(Parcel in) {
        mPackageName = in.readString();
        mClassName = in.readString();
        mMethodName = in.readString();
        mMethodParamTypes = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPackageName);
        dest.writeString(mClassName);
        dest.writeString(mMethodName);
        dest.writeStringArray(mMethodParamTypes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrackedMethod> CREATOR = new Creator<TrackedMethod>() {
        @Override
        public TrackedMethod createFromParcel(Parcel in) {
            return new TrackedMethod(in);
        }

        @Override
        public TrackedMethod[] newArray(int size) {
            return new TrackedMethod[size];
        }
    };

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String packageName) {
        mPackageName = packageName;
    }

    public String getClassName() {
        return mClassName;
    }

    public void setClassName(String className) {
        mClassName = className;
    }

    public String getMethodName() {
        return mMethodName;
    }

    public void setMethodName(String methodName) {
        mMethodName = methodName;
    }

    public String[] getMethodParamTypes() {
        return mMethodParamTypes;
    }

    public void setMethodParamTypes(String[] methodParamTypes) {
        mMethodParamTypes = methodParamTypes;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(mPackageName) && !TextUtils.isEmpty(mClassName);
    }

    /**
     * 检查给定的参数列表类型是否满足
     *
     * @param types  需要匹配的参数列表数组
     * @return 匹配成功返回true，否则false
     */
    public boolean matchMethodParamTypes(Class<?>[] types) {
        String[] target = mMethodParamTypes;
        if (target == null) {
            return true;
        }

        if (types == null || target.length != types.length) {
            return false;
        }

        for (int i = 0; i < target.length; i++) {
            if (!TextUtils.equals(target[i], types[i].getName())) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return 返回方法与方法参数组合成的的字符串表示形式
     */
    public String toMethodString() {
        return (TextUtils.isEmpty(mMethodName) ? "constructor" : mMethodName) +
            "(" + (mMethodParamTypes != null ? TextUtils.join(", ", mMethodParamTypes) : "*") + ")";
    }

    @Override
    public String toString() {
        return "TrackedMethod{" +
            "mPackageName='" + mPackageName + '\'' +
            ", mClassName='" + mClassName + '\'' +
            ", mMethodName='" + mMethodName + '\'' +
            ", mMethodParamTypes=" + Arrays.toString(mMethodParamTypes) +
            '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof TrackedMethod
            && Objects.equals(mPackageName, ((TrackedMethod) obj).mPackageName)
            && Objects.equals(mClassName, ((TrackedMethod) obj).mClassName)
            && Objects.equals(mMethodName, ((TrackedMethod) obj).mMethodName)
            && Arrays.equals(mMethodParamTypes, ((TrackedMethod) obj).mMethodParamTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPackageName, mClassName, mMethodName, Arrays.hashCode(mMethodParamTypes));
    }
}
