package eu.transkribus.util;

import java.util.Objects;

@FunctionalInterface
public interface CheckedConsumer<T> {
 	void accept(T t) throws Exception;
 	default CheckedConsumer<T>	andThen(CheckedConsumer<? super T> after) throws Exception {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
 	}
}
