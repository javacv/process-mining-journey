# Journey Sticking - Kafka + Elasticsearch
## Docker Compose
docker compose up -d

# KAFKA

docker exec -it <kafka_container_id> kafka-topics --create \
--bootstrap-server localhost:9092 \
--topic raw-events \
--partitions 6 \
--replication-factor 1


# Elastic Search Index journeys-v1

curl -X PUT "http://localhost:9200/journeys-v1" \
-H 'Content-Type: application/json' \
-d '{
"mappings": {
"properties": {
"journeyId": { "type": "keyword" },
"cks": { "type": "keyword" },
"eventIds": { "type": "keyword" },
"firstSeenAt": { "type": "date" },
"lastSeenAt": { "type": "date" },
"status": { "type": "keyword" },
"counters": { "type": "object", "enabled": true }
}
}
}'


# Elastic Search Index ckmap
curl -X PUT "http://localhost:9200/ckmap" \
-H 'Content-Type: application/json' \
-d '{
"mappings": {
"properties": {
"ck": { "type": "keyword" },
"journeyId": { "type": "keyword" },
"lastUpdated": { "type": "date" }
}
}
}'


## Send test events to Kafka
kafka-console-producer --broker-list localhost:9092 --topic events.raw
```
{"eventId":"E11","activity":"Application Submitted","correlationKeys":["CK1","CK3"],"timestamp":"2025-08-15T20:00:00Z"}
{"eventId":"E12","activity":"Identity Verification","correlationKeys":["CK1","CK3"],"timestamp":"2025-08-15T20:05:00Z"}
{"eventId":"E13","activity":"Credit Check","correlationKeys":["CK1","CK4"],"timestamp":"2025-08-15T20:10:00Z"}
```

## Verify in Elasticsearch

```
curl -s http://localhost:9200/journeys-v1/_search\?pretty
```

You should see your journey document with:

journeyId assigned

cks list containing CK1, CK3, CK4

eventIds list containing E11, E12, E13