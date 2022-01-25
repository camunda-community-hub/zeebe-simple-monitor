package io.zeebe.monitor.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

abstract class AbstractViewController {

  private static final int FIRST_PAGE = 0;
  private static final int PAGE_RANGE = 2;

  @Autowired private WhitelabelProperties whitelabelProperties;
  @Autowired private WhitelabelPropertiesMapper whitelabelPropertiesMapper;

  protected void addPaginationToModel(
      final Map<String, Object> model, final Pageable pageable, final long count) {

    final int currentPage = pageable.getPageNumber();
    final int prevPage = currentPage - 1;
    final int nextPage = currentPage + 1;
    final int lastPage = getLastPage(pageable, count);

    final var prevPages =
        IntStream.range(currentPage - PAGE_RANGE, currentPage)
            .filter(p -> p > FIRST_PAGE)
            .boxed()
            .map(Page::new)
            .collect(Collectors.toList());
    final var nextPages =
        IntStream.rangeClosed(currentPage + 1, currentPage + PAGE_RANGE)
            .filter(p -> p < lastPage)
            .boxed()
            .map(Page::new)
            .collect(Collectors.toList());
    final var hasPrevGap =
        !prevPages.isEmpty() && prevPages.stream().allMatch(p -> p.pageNumber > FIRST_PAGE + 1);
    final var hasNextGap =
        !nextPages.isEmpty() && nextPages.stream().allMatch(p -> p.pageNumber < lastPage - 1);

    model.put("page", new Page(currentPage));
    model.put("pageSize", pageable.getPageSize());
    model.put("prevPages", prevPages);
    model.put("nextPages", nextPages);
    model.put("hasPrevPagesGap", hasPrevGap);
    model.put("hasNextPagesGap", hasNextGap);

    if (currentPage > 0) {
      model.put("prevPage", new Page(prevPage));
      model.put("firstPage", new Page(FIRST_PAGE));
    }
    if (lastPage > currentPage) {
      model.put("nextPage", new Page(nextPage));
      model.put("lastPage", new Page(lastPage));
    }
  }

  private int getLastPage(final Pageable pageable, final long count) {
    int lastPage = 0;
    if (pageable.getPageSize() > 0) {
      lastPage = (int) count / pageable.getPageSize();
      if (count % pageable.getPageSize() == 0) {
        lastPage--;
      }
    }
    return lastPage;
  }

  /*
   * Needs to be added manually, since Spring does not detect @ModelAttribute in abstract classes.
   */
  protected void addDefaultAttributesToModel(Map<String, Object> model) {
    whitelabelPropertiesMapper.addPropertiesToModel(model, whitelabelProperties);
  }

  private static class Page {
    private final int pageNumber;
    private final int displayNumber;

    private Page(final int pageNumber) {
      this.pageNumber = pageNumber;
      this.displayNumber = pageNumber + 1;
    }

    public int getPageNumber() {
      return pageNumber;
    }

    public int getDisplayNumber() {
      return displayNumber;
    }
  }
}
