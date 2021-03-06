# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\ProgramData\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


# network models
-keep class com.mediatek.mt6381eco.network.model.** { *; }

#room entries
-keep class com.mediatek.mt6381eco.db.entries.** { *; }

#fix duplicate definition of library class [org.apache.http.**]
-dontnote android.net.http.*
-dontnote org.apache.commons.codec.**
-dontnote org.apache.http.**

#dragger2
-dontwarn com.google.errorprone.annotations.*

#okhttp
-dontwarn okio.**

#retrofit2
-dontwarn retrofit2.Platform$Java8

#saripaar
-keep class com.mobsandgeeks.saripaar.** {*;}
-keep @com.mobsandgeeks.saripaar.annotation.ValidateUsing class * {*;}

#jni
-keep class com.mediatek.jni.mt6381.** {*;}

#6381 data model
-keep class com.mediatek.mt6381eco.biz.peripheral.SensorData {*;}