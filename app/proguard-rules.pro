# kotlinx.serialization: mantiene i serializer generati per i modelli salvati.
-keepclassmembers @kotlinx.serialization.Serializable class com.sblorftz.timer.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
