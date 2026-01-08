package org.umc.valuedi.global.external.codef.dto;

public record CodefResponseEnvelope(
        boolean success,
        String message,
        String raw
) {
    public static CodefResponseEnvelope ok(String raw) {
        return new CodefResponseEnvelope(true, "OK", raw);
    }

    public static CodefResponseEnvelope fail(String message, String rawOrNull) {
        return new CodefResponseEnvelope(false, message, rawOrNull);
    }
}
