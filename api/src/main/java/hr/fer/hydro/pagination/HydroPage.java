package hr.fer.hydro.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HydroPage<T> {
    private List<T> data;
    private Integer numOfPages;
    private Integer elementsInPage;
    private boolean isLastPage;
    private boolean isFirstPage;
}
