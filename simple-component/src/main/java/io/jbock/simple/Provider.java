package io.jbock.simple;

/**
 * Provides instances of {@code T}.
 * For any type {@code T} that can be injected, you can also inject {@code Provider<T>}.
 * Compared to injecting {@code T} directly, injecting {@code Provider<T>} enables:
 * 
 * <ul>
 *     <li>retrieving multiple instances.</li>
 *     <li>lazy or optional retrieval of an instance.</li>
 * </ul> 
 */
public interface Provider<T> {

    /**
     * Provides a fully-constructed and injected instance of {@code T}. 
     */
    T get();
}
