from argparse import ArgumentParser
from sys import stdin, stderr
from time import time

from daemon import DAEMON_MAIN
from daemon.config import REPOSITORY_PATHS
from daemon.network import Connection
from daemon.scm.git import Git
from daemon.utils import make_tempfile, delete_tempfile

OPTIONS = {
    'filename': str,
    'start': int,
    'end': int,
}

def main():
    parser = ArgumentParser(
        prog=DAEMON_MAIN,
        usage='%(prog)s [<args>]',
        add_help=False)

    for k, v in OPTIONS.iteritems():
        parser.add_argument('--' + k, type=v)

    opt = parser.parse_args()

    repo_path, server_url = repo_url(opt.filename)

    if server_url is None:
        return

    try:
        git = Git(opt.filename, repo_path)
    except Exception as e:
        stderr.write(str(e))
        return

    current_file = make_tempfile(stdin.read())

    diff = git.diff(new=current_file)
    lines = git.apply_diff(diff=diff, start=opt.start, end=opt.end)

    delete_tempfile(current_file)
    delete_tempfile(git.original_file)

    conn = Connection(server_url)

    try:
        conn.post(path = "/api/snapshot", payload = {
            "timestamp": int(time() * 1000),
            "file": git.relative_file_path.strip(),
            "email": git.email.strip(),
            "commit": git.commit,
            "lines[]": lines,
        })
    except Exception as e:
        stderr.write(str(e))
        return


def repo_url(path):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for k, v in REPOSITORY_PATHS.iteritems():
        if path.startswith(k):
            return k, v

    return None
