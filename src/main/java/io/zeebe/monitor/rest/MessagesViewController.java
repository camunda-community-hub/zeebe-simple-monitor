package io.zeebe.monitor.rest;

import io.zeebe.monitor.entity.MessageEntity;
import io.zeebe.monitor.repository.MessageRepository;
import io.zeebe.monitor.rest.dto.MessageDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MessagesViewController extends AbstractViewController {

  @Autowired private MessageRepository messageRepository;

  @GetMapping("/views/messages")
  public String messageList(final Map<String, Object> model, final Pageable pageable) {

    final long count = messageRepository.count();

    final List<MessageDto> dtos = new ArrayList<>();
    for (final MessageEntity messageEntity : messageRepository.findAll(pageable)) {
      final MessageDto dto = toDto(messageEntity);
      dtos.add(dto);
    }

    model.put("messages", dtos);
    model.put("count", count);

    addPaginationToModel(model, pageable, count);
    addDefaultAttributesToModel(model);

    return "message-list-view";
  }

  private MessageDto toDto(final MessageEntity message) {
    final MessageDto dto = new MessageDto();

    dto.setKey(message.getKey());
    dto.setName(message.getName());
    dto.setCorrelationKey(message.getCorrelationKey());
    dto.setMessageId(message.getMessageId());
    dto.setPayload(message.getPayload());
    dto.setState(message.getState());
    dto.setTimestamp(Instant.ofEpochMilli(message.getTimestamp()).toString());

    return dto;
  }
}
