 #!/usr/bin/env bash

git pull
pkill java
          nohup ./scripts/run_instance.sh sonicle 80 prod > /dev/null
sleep 10; nohup ./scripts/run_instance.sh accio 9000 prod > /dev/null
