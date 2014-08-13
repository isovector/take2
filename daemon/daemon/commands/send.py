from json import dumps
from os.path import normpath
from sys import stdout, stderr, stdin
from time import time

from daemon.config import REPOSITORY_PATHS
from daemon.network import Connection
from daemon.parser import build_parser
from daemon.scm.diff import create_diff, convert_line_numbers
from daemon.scm.git import Git


OPTIONS = {
    '--filename': str,
    '--repo_path': str,
    '--server_url': str,
    '--start': int,
    '--end': int,
    '--debug': bool,
}


def repo_url(path):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for k, v in REPOSITORY_PATHS.iteritems():
        if normpath(path).startswith(normpath(k)):
            return k, v

    return None, None


def send(args):
    parser = build_parser(OPTIONS)
    opt = parser.parse_args(args=args)

    repo_path, server_url = repo_url(opt.filename)

    if opt.repo_path:
        repo_path = opt.repo_path

    if opt.repo_path:
        server_url = opt.server_url

    if server_url is None:
        return

    try:
        git = Git(repo_path)
    except Exception as e:
        stderr.write(str(e))
        return

    rel_filename = git.relative_file_path(opt.filename)
    commit = git.get_last_pushed_commit()

    lines = convert_line_numbers(
        create_diff(
            old_content=stdin.read(),
            new_content=git.get_file_content(rel_filename, commit)),
        lines=range(opt.start, opt.end + 1))
    lines = [x for x in lines if x is not None]

    payload = {
        "timestamp": int(time() * 1000),
        "file": rel_filename,
        "name": git.name,
        "email": git.email,
        "branch": git.branch,
        "commit": commit,
        "lines[]": lines,
    }

    if opt.debug:
        stdout.write(dumps(payload))
        return

    try:
        conn = Connection(server_url)
        conn.post(path="/api/snapshot", payload=payload)
    except Exception as e:
        stderr.write(str(e))
        return
