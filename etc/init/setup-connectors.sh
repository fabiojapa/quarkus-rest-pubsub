#!/bin/bash
# Wait for the Connect to start
#apt update
#apt install -y curl gettext

echo "Waiting for Connect to be ready"
until curl -s http://connect:8083/ &> /dev/null
do
  echo -n "."
  sleep 1
done
sleep 5

envsubst < /connector/register-sqlserver.json > register-sqlserver-temp.json
envsubst < /connector/sink-connector.json > sink-connector-temp.json
curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://connect:8083/connectors/ -d @register-sqlserver-temp.json

curl -X POST -H "Content-Type: application/json" -d @sink-connector-temp.json http://connect:8083/connectors
