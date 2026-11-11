package com.brochacho.dmzgrowth.namek;

/** true solo mientras DMZ procesa la RESPUESTA ACEPTADA del objetivo; evita asimilar sin consentimiento. */
public final class ConsentFlag {
    public static final ThreadLocal<Boolean> ACCEPTED = ThreadLocal.withInitial(() -> false);
    private ConsentFlag() {}
}
