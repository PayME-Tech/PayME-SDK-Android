package vn.payme.sdk.api;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

public class ConvertJSON {
    public String toString(String dataRaw) {
        dataRaw = dataRaw.replace("\\r","");
        dataRaw = dataRaw.replace("\\n","");
        dataRaw = dataRaw.replaceAll("\\\\\"","\"");
        dataRaw = dataRaw.replaceAll("\\\\\\\"","\"");
        dataRaw = dataRaw.replace("\\\\","\\");
        return dataRaw.substring(1, dataRaw.length() - 1);
    }
}
