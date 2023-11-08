package io.zeebe.monitor.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@ConditionalOnProperty(value="mode", havingValue = "elastic")
public class ZeebeElasticService {

  private static final Logger LOG = LoggerFactory.getLogger(ZeebeElasticService.class);

  @Value("${elastic.url}")
  private String elasticUrl;

  @Value("${elastic.user}")
  private String elasticUser;

  @Value("${elastic.password}")
  private String elasticPassword;


  private Lock lock = new ReentrantLock();
  @Autowired private ZeebeElasticImportService importService;

  private ElasticsearchClient client;
  private ElasticsearchTransport transport;

  public void buildClient() {
    final CredentialsProvider credentialsProvider =
            new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
            new UsernamePasswordCredentials(elasticUser, elasticPassword));

    RestClient restClient = RestClient
            .builder(HttpHost.create(elasticUrl))
            .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                    .setDefaultCredentialsProvider(credentialsProvider))
            .build();
    transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
    client = new ElasticsearchClient(transport);
    LOG.info("Connecting to ElasticSearch '{}'", elasticUrl);
  }

  @Scheduled(fixedRate = 3_000L, initialDelay = 10_000)
  public void importFromElastic() throws Exception {
    if (client == null) {
      lock.lock();
      if (client == null) {
        buildClient();
      }
      lock.unlock();
    }
    importService.importFrom(client);
  }

  @PreDestroy
  public void close() throws Exception {
    if (transport != null) {
      transport.close();
    }
  }
}
