package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.ErrorEntity;
import io.zeebe.monitor.repository.ErrorRepository;
import io.zeebe.monitor.rest.dto.ErrorDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorsViewController extends AbstractViewController {

  @Autowired private ErrorRepository errorRepository;

  @GetMapping("/views/errors")
  public String errorList(final Map<String, Object> model, final Pageable pageable) {

    final long count = errorRepository.count();

    final List<ErrorDto> dtos = new ArrayList<>();
    for (final ErrorEntity entity : errorRepository.findAll(pageable)) {
      final var dto = toDto(entity);
      dtos.add(dto);
    }

    model.put("errors", dtos);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "error-list-view";
  }

  static ErrorDto toDto(final ErrorEntity entity) {
    final var dto = new ErrorDto();
    dto.setPosition(entity.getPosition());
    dto.setErrorEventPosition(entity.getErrorEventPosition());
    dto.setExceptionMessage(entity.getExceptionMessage());
    dto.setStacktrace(entity.getStacktrace());
    dto.setTimestamp(Instant.ofEpochMilli(entity.getTimestamp()).toString());

    if (entity.getProcessInstanceKey() > 0) {
      dto.setProcessInstanceKey(entity.getProcessInstanceKey());
    }

    return dto;
  }
}
