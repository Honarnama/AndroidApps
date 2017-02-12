-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

-keepattributes *Annotation*
-keepattributes Signature
-dontwarn android.net.SSLCertificateSocketFactory
-dontwarn android.app.Notification
-dontwarn com.squareup.**
-dontwarn okio.**

-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable,*Annotation*

-keep class .R
-keep class **.R$* {
    <fields>;
}

-dontwarn sun.misc.Unsafe

-dontwarn com.google.common.**
-dontwarn org.mockito.**
-dontwarn sun.reflect.**
-dontwarn android.test.**

# Need to create channel through service provider.
-keepnames class io.grpc.ManagedChannelProvider
-keep class io.grpc.okhttp.OkHttpChannelProvider

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class io.grpc.internal.DnsNameResolverProvider
-keep class io.grpc.okhttp.OkHttpChannelProvider

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# for DexGuard only
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule