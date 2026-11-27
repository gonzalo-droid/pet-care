# Reglas para cuando se active R8 (hoy isMinifyEnabled = false). Al activarlo, probar el release entero.

# DataStore Preferences (TRP-01): su protobuf interno lee los campos de GeneratedMessageLite por reflexión.
# Sin esto, R8 (modo completo) los elimina y la primera lectura de preferencias nunca emite.
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# Crashlytics (CRA-02): conservar archivo y línea para que el mapping de R8 dé trazas legibles.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
