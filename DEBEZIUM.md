# Debezium Tutorial
from [debezium](https://github.com/debezium/debezium-examples/blob/main/tutorial/README.md)

This demo automatically deploys the topology of services as defined in the [Debezium Tutorial](https://debezium.io/documentation/reference/stable/tutorial.html), but additionally includes a Mongodb and MongoSinkConnector.

- [Debezium Tutorial](#debezium-tutorial)
    
    * [Using this Stack](#using-this-stack)
    * [Debugging](#debugging)


## Using this Stack

```shell
# Start the topology as defined in https://debezium.io/documentation/reference/stable/tutorial.html
docker compose -f docker-compose-sqlserver.yaml up

# Connect to SQL Server
docker compose -f docker-compose-sqlserver.yaml exec sqlserver bash -c '/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD -d testDB'
# Modify records in the database via SQL Server client (do not forget to add `GO` command to execute the statement)
INSERT INTO products(name,description,weight) VALUES ('test','xpto',3.14);
GO

# connect to mongo
docker compose -f docker-compose-sqlserver.yaml exec  mongo mongosh --username root --password example
# list records to see if data is flowing from sql server
use local
db.products.find()
it

# Shut down the cluster
docker-compose -f docker-compose-sqlserver.yaml down
```



## Debugging

- [kafdrop](http://localhost:19000/)
- [kafka-connectors](http://localhost:8083/connectors)
- [mongo-express](http://localhost:18081/)
  - admin / admin


## Notes
The installation is automated, but these are the commands in the automation:

```shell
# Initialize database and insert test data used in container sqlserver-script-executor: etc/debezium-sqlserver-init/inventory.sql
cat etc/debezium-sqlserver-init/inventory.sql | docker-compose -f docker-compose-sqlserver.yaml exec -T sqlserver bash -c '/opt/mssql-tools/bin/sqlcmd -U sa -P $SA_PASSWORD'

# Start the sqlserver connector in container setup-connectors: etc/connector/register-sqlserver.json and etc/connector/sink-connector.json
export $(cat .env | xargs)
envsubst < etc/connector/register-sqlserver.json > etc/connector/register-sqlserver-temp.json
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://localhost:8083/connectors/ -d @etc/connector/register-sqlserver-temp.json

# Start the mongo sink connector in container setup-connectors: etc/connector/sink-connector.json
export $(cat .env | xargs)
envsubst < etc/connector/sink-connector.json > etc/connector/sink-connector-temp.json
curl -X POST -H "Content-Type: application/json" -d @etc/connector/sink-connector-temp.json http://localhost:8083/connectors | jq
```

## References
[Debezium Architecture](https://debezium.io/documentation/reference/stable/architecture.html)