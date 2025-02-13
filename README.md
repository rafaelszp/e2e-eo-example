### info

- hdfs web ui: http://localhost:50070/
- rabbitmq web ui: http://localhost:15672

### setup

```shell
docker exec -it hdfs hdfs dfs -mkdir /checkpoints
docker exec -it hdfs hdfs dfs -chown $USER /checkpoints

```


## TODO

- [ ] Gerar Fluxo e2e EO com
    - [ ] custom source baseado em rabbitmq
    - [ ] processor com random error
    - [ ] sink em arquivo, modelo append, ou até mesmo outra fila rabbitmq
    - [ ] checkpoints com rocksdb
    - [ ] hdfs baseado em docker para simular state distribuído


### references

- https://github.com/apache/flink/blob/release-1.20.0/flink-core/src/test/java/org/apache/flink/api/connector/source/mocks/MockSplitEnumerator.java
- https://cwiki.apache.org/confluence/display/FLINK/FLIP-27%3A+Refactor+Source+Interface
- https://github.com/SelimAbidin/CustomFlinkSource/blob/V1/src/main/scala/org/example/custom/source/IntEnumerator.scala
- https://medium.com/@SelimAbidin/how-flink-sources-work-and-how-to-implement-one-70b52fcfeb29
- https://medium.com/@ahmetcanozturk1996/setting-up-a-hdfs-cluster-on-docker-a-practical-guide-cc13fa42ca52
- https://stackoverflow.com/a/71322538
- https://community.cloudera.com/t5/Support-Questions/Permission-denied-user-ABC-access-WRITE-inode-quot-user-quot/m-p/90922