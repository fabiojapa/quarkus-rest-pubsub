#!/bin/bash
apt update
apt install -y curl

curl -i -X POST -H "Accept:application/json" -H  "Content-Type:application/json" http://connect:8083/connectors/ -d @/connector/register-sqlserver.json

curl -X POST -H "Content-Type: application/json" -d @/connector/sink-connector.json http://connect:8083/connectors | jq
