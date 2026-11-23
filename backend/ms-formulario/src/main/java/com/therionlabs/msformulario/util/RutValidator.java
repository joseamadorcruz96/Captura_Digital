package com.therionlabs.msformulario.util;

/**
 * Validador y normalizador de RUT chileno (Módulo 11), alineado con RutUtils.kt del cliente móvil.
 */
public final class RutValidator {

    private RutValidator() {
    }

    public static String clean(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replace(".", "")
                .replace("-", "")
                .trim()
                .toUpperCase();
    }

    public static boolean isValid(String rut) {
        String cleaned = clean(rut);
        if (cleaned.length() < 8 || cleaned.length() > 9) {
            return false;
        }
        String cuerpo = cleaned.substring(0, cleaned.length() - 1);
        char dv = cleaned.charAt(cleaned.length() - 1);

        if (!cuerpo.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return calculateDv(cuerpo) == dv;
    }

    /**
     * Formatea el RUT al estándar XX.XXX.XXX-X para asegurar unicidad canónica en base de datos.
     */
    public static String normalize(String rut) {
        String cleaned = clean(rut);
        if (cleaned.length() < 8) {
            return cleaned;
        }
        String limited = cleaned.length() > 9 ? cleaned.substring(0, 9) : cleaned;
        String cuerpo = limited.substring(0, limited.length() - 1);
        char dv = limited.charAt(limited.length() - 1);

        StringBuilder formatted = new StringBuilder();
        int count = 0;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            formatted.append(cuerpo.charAt(i));
            count++;
            if (count == 3 && i > 0) {
                formatted.append('.');
                count = 0;
            }
        }
        return formatted.reverse() + "-" + dv;
    }

    public static char calculateDv(String cuerpo) {
        int sum = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int digit = cuerpo.charAt(i) - '0';
            sum += digit * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int remainder = sum % 11;
        int result = 11 - remainder;
        return switch (result) {
            case 11 -> '0';
            case 10 -> 'K';
            default -> (char) ('0' + result);
        };
    }
}
