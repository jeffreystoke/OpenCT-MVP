# for retrofit 2
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
# for Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class net.fortuna.ical4j.** { *; }

-dontwarn javax.annotation.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
# for iCal4J
-dontwarn net.fortuna.ical4j.model.**
-dontwarn org.slf4j.impl.**
-dontwarn groovy.**
-dontwarn org.codehaus.groovy.**
-dontwarn org.apache.commons.logging.**
-dontwarn sun.misc.Perf
-dontwarn aQute.bnd.**

#-dontnote com.google.vending.**
#-dontnote com.android.vending.licensing.**

-keepnames class ** { *; }
-keepnames interface ** { *; }
-keepnames enum ** { *; }

-keep class org.mozilla.javascript.** { *; }
-dontwarn org.mozilla.classfile.**
-dontwarn org.mozilla.javascript.**