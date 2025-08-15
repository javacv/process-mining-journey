


#KAFKA

## Docker Compose
docker compose up -d

##
docker exec -it $(docker ps -qf "name=kafka") \
kafka-topics --create \
--bootstrap-server localhost:9092 \
--topic events.raw \
--partitions 6 \
--replication-factor 1




# Elastic Search

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


#CK Map
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