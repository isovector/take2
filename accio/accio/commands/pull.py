from sys import stdout, stderr, stdin
from time import time
import os

from accio.common import repo_url
from accio.network import Connection


def getCoeffsForFile(filename):
    repo_path, server_url, filename = repo_url(filename)

    def getLocalFile(filename):
        return repo_path + os.sep + filename

    if server_url is None:
        return False

    print(filename)

    json = None
    try:
        conn = Connection(server_url)
        json = conn.get("/api/coefficients/" + filename).json()
    except Exception as e:
        stderr.write(str(e))
        return None

    return map(
        lambda x: (getLocalFile(x["destination"]), x["coefficient"]),
        json
    )
