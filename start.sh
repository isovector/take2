 #!/usr/bin/env bash

git pull
pkill java
          nohup ./scripts/run_instance.sh accio   80 > /dev/null
# sleep 10; nohup ./scripts/run_instance.sh accio 9001 > /dev/null
