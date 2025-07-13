package io.authid.core.shared.components.database.factory;

public interface HasFactory<F extends Factory<?>> {
    static <F extends Factory<?>> F factory() {
        throw new UnsupportedOperationException("Implement this method in your model.");
    }
}