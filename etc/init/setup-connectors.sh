#!/bin/bash
# Wait for the Connect to start
echo "Waiting for Connect to be ready"
until curl -s http://connect:8083/ &> /dev/null
do
  echo -n "."
  sleep 1
done
sleep 7
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://connect:8083/connectors/ -d @/connector/register-sqlserver.json

curl -X POST -H "Content-Type: application/json" -d @/connector/sink-connector.json http://connect:8083/connectors | jq
