# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces. (for crashlytics)
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
# Add this global rule for firebase realtime db
-keepattributes Signature
# for firebase auth
-keepattributes *Annotation*

# --For firebase realtime db--
# This rule will properly ProGuard all the model classes in
# the package in.lubble.app.models
-keepclassmembers class in.lubble.app.models.** {
  *;
}
# crashlytics
-keep public class * extends java.lang.Exception
-keep class org.jsoup.**
-dontwarn com.google.firebase.appindexing.**
-dontwarn com.android.installreferrer.api.**