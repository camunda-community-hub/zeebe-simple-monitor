package io.zeebe.monitor.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WhitelabelPropertiesMapperTest {

  private WhitelabelPropertiesMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new WhitelabelPropertiesMapper();
  }

  @Test
  void properties_mapping_is_in_synch_between_both_overloaded_methods() {
    final WhitelabelProperties properties = new WhitelabelProperties(
        "basePath",
        "logoPath",
        "customTitle",
        "customCssPath",
        "customJsPath");

    Model model = new ModelMock();
    Map<String, Object> modelMap = new HashMap<>();
    mapper.addPropertiesToModel(model, properties);
    mapper.addPropertiesToModel(modelMap, properties);

    assertThat(modelMap).isEqualTo(model.asMap());
  }

  private static class ModelMock implements Model {

    final Map<String, Object> attributes = new HashMap<>();

    @Override
    public Model addAttribute(String attributeName, Object attributeValue) {
      attributes.put(attributeName, attributeValue);
      return this;
    }

    @Override
    public Model addAttribute(Object attributeValue) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Model addAllAttributes(Collection<?> attributeValues) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Model addAllAttributes(Map<String, ?> attributes) {
      this.attributes.putAll(attributes);
      return this;
    }

    @Override
    public Model mergeAttributes(Map<String, ?> attributes) {
      throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean containsAttribute(String attributeName) {
      return attributes.containsKey(attributeName);
    }

    @Override
    public Object getAttribute(String attributeName) {
      return attributes.get(attributeName);
    }

    @Override
    public Map<String, Object> asMap() {
      return Collections.unmodifiableMap(attributes);
    }
  }
}
