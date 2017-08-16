#!/bin/bash

sed -i "s/ZEEBE_HOST/${ZEEBE_HOST}/g" /usr/local/zeebe/conf/zeebe.cfg.toml

exec ./broker run
