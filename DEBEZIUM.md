# Debezium Tutorial
from [debezium](https://github.com/debezium/debezium-examples/blob/main/tutorial/README.md)

This demo automatically deploys the topology of services as defined in the [Debezium Tutorial](https://debezium.io/documentation/reference/stable/tutorial.html).

- [Debezium Tutorial](#debezium-tutorial)
    
    * [Using SQL Server](#using-sql-server)
    * [Using externalized secrets](#using-externalized-secrets)
    * [Running without ZooKeeper](#running-without-zookeeper)
    * [Debugging](#debugging)


## Using SQL Server

```shell
# Start the topology as defined in https://debezium.io/documentation/reference/stable/tutorial.html
docker-compose -f docker-compose-sqlserver.yaml up

# Initialize database and insert test data
cat etc/debezium-sqlserver-init/inventory.sql | docker-compose -f docker-compose-sqlserver.yaml exec -T sqlserver bash -c '/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD'

# Start SQL Server connector
export $(cat .env | xargs)
envsubst < etc/connector/register-sqlserver.json > etc/connector/register-sqlserver-temp.json
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @etc/connector/register-sqlserver-temp.json

# Consume messages from a Debezium topic
#docker-compose -f docker-compose-sqlserver.yaml exec kafka /kafka/bin/kafka-console-consumer.sh \
#    --bootstrap-server kafka:9092 \
#    --from-beginning \
#    --property print.key=true \
#    --topic server1.testDB.dbo.customers
export $(cat .env | xargs)
envsubst < etc/connector/sink-connector.json > etc/connector/sink-connector-temp.json
curl -X POST -H "Content-Type: application/json" -d @etc/connector/sink-connector-temp.json http://localhost:8083/connectors | jq

# Modify records in the database via SQL Server client (do not forget to add `GO` command to execute the statement)
docker-compose -f docker-compose-sqlserver.yaml exec sqlserver bash -c '/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD -d testDB'

# Shut down the cluster
docker-compose -f docker-compose-sqlserver.yaml down
```



## Debugging

Should you need to establish a remote debugging session into a deployed connector, add the following to the `environment` section of the `connect` in the Compose file service:

    - KAFKA_DEBUG=true
    - DEBUG_SUSPEND_FLAG=n
    - JAVA_DEBUG_PORT=*:5005

Also expose the debugging port 5005 under `ports`:

    - 5005:5005

You can then establish a remote debugging session from your IDE on localhost:5005.


kafdrop: http://localhost:19000/
kafka-connectors: http://localhost:8083/connectors
mongo-express: http://localhost:18081/