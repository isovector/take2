from json import dumps
from sys import stdout, stderr, stdin
from time import time

from daemon.common import repo_url
from daemon.network import Connection
from daemon.scm.diff import create_diff, convert_line_numbers
from daemon.scm.git import Git


def send(filename, start, end, buffer, debug = False):
    repo_path, server_url, filename = repo_url(filename)

    if server_url is None:
        return False

    try:
        git = Git(repo_path)
    except Exception as e:
        stderr.write(str(e))
        return

    commit = git.get_last_pushed_commit()

    if buffer:
        f = open(buffer, "r")
        wip = f.read()
    else:
        wip = stdin.read()

    lines = convert_line_numbers(
        create_diff(
            old_content=wip,
            new_content=git.get_file_content(filename, commit)),
        lines=range(start, end + 1))
    lines = [x for x in lines if x is not None]

    payload = {
        "timestamp": int(time() * 1000),
        "file": filename,
        "name": git.name,
        "email": git.email,
        "branch": git.branch,
        "commit": commit,
        "lines[]": lines,
    }

    if debug:
        stdout.write(dumps(payload))
        print ""
        return False

    try:
        conn = Connection(server_url)
        conn.post(path="/api/snapshot", payload=payload)
        return True
    except Exception as e:
        stderr.write(str(e))
        return False
