FROM camunda/zeebe:0.1.1
ADD ./zeebe.cfg.toml /usr/local/zeebe/conf/zeebe.cfg.toml

ADD ./configure_and_run.sh ./
ENTRYPOINT ["./configure_and_run.sh"]
