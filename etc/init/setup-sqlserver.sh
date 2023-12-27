#!/bin/bash
# Wait for the SQL Server to start
echo "Waiting for SQL Server to be ready"
until /opt/mssql-tools/bin/sqlcmd -S sqlserver -U sa -P $SA_PASSWORD -Q "SELECT 1" &> /dev/null
do
  echo -n "."
  sleep 1
done

echo "SQL Server is up - executing command"
/opt/mssql-tools/bin/sqlcmd -S sqlserver -U sa -P $SA_PASSWORD -i /scripts/inventory.sql
