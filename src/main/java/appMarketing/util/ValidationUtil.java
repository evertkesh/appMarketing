package appMarketing.util;

import java.util.Collection;

public class ValidationUtil {

    /**
     * Valida que un objeto no sea nulo
     * @param object Objeto a validar
     * @param message Mensaje de error personalizado
     * @throws IllegalArgumentException si el objeto es nulo
     */
    public static void requireNonNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que una colección no sea nula y no esté vacía
     * @param collection Colección a validar
     * @param message Mensaje de error personalizado
     * @throws IllegalArgumentException si la colección es nula o vacía
     */
    public static void requireNonEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que una cadena de texto no sea nula ni vacía
     * @param text Texto a validar
     * @param message Mensaje de error personalizado
     * @throws IllegalArgumentException si el texto es nulo o vacío
     */
    public static void requireNonBlank(String text, String message) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Valida que un ID no sea nulo ni inválido
     * @param id ID a validar
     * @param message Mensaje de error personalizado
     * @throws IllegalArgumentException si el ID es nulo o negativo
     */
    public static void requireValidId(Long id, String message) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
