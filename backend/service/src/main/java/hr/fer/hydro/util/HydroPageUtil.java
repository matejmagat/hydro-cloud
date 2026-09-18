package hr.fer.hydro.util;

import hr.fer.hydro.pagination.HydroPage;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class HydroPageUtil {
    public static <T> HydroPage<T> toPage(final Page<T> page) {
        return new HydroPage<>(
                page.getContent(),
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isLast(),
                page.isFirst()
        );
    }

    public static <T, R> HydroPage<R> toPage(final Page<T> page, final Function<T, R> mapper) {
        final List<R> mappedData = page.getContent().stream()
                .map(mapper)
                .toList();
        return new HydroPage<>(
                mappedData,
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.isLast(),
                page.isFirst()
        );
    }


}
