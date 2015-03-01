from json import dumps
from sys import stdout, stderr, stdin
from time import time
from os.path import relpath

from accio.common import repo_url
from accio.network import Connection
from accio.scm.diff import create_diff, convert_line_numbers
from accio.scm.git import Git


def send(filename, start, end, buffer, debug=False, use_repo_path=None):
    repo_path, server_url, new_filename = repo_url(filename)

    if use_repo_path:
        # we want to override the repo_path for some reason (testing usually)
        repo_path = use_repo_path

    if new_filename:
        filename = new_filename

    if use_repo_path:
        # if we have overriden it, our filepath is no longer relative
        filename = relpath(filename, repo_path)

    if server_url is None and not debug:
        return False

    try:
        git = Git(repo_path)
    except Exception as e:
        stderr.write(str(e))
        return None

    commit = git.get_last_pushed_commit()

    f = open(buffer, "r")
    wip = f.read()

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

    if not debug:
        try:
            conn = Connection(server_url)
            conn.post(path="/api/snapshot", payload=payload)
        except Exception as e:
            stderr.write(str(e))
            return False
    return dumps(payload)
