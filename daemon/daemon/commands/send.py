from json import dumps
from os.path import normpath
import os
from os import path
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
    '--buffer': str,
}


def walk_up(bottom):
    """
    mimic os.walk, but walk 'up'
    instead of down the directory tree
    from: https://gist.github.com/zdavkeos/1098474
    """

    bottom = path.realpath(bottom)

    #get files in current dir
    try:
        names = os.listdir(bottom)
    except Exception as e:
        print e
        return


    dirs, nondirs = [], []
    for name in names:
        if path.isdir(path.join(bottom, name)):
            dirs.append(name)
        else:
            nondirs.append(name)

    yield bottom, dirs, nondirs

    new_path = path.realpath(path.join(bottom, '..'))

    # see if we are at the top
    if new_path == bottom:
        return

    for x in walk_up(new_path):
        yield x


def repo_url(path):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for dircontents in walk_up(path):
        for filename in dircontents[2]:
            dirname = dircontents[0]
            if filename == ".take2rc":
                with open(dirname + os.sep + filename) as f:
                    return dirname, f.read()
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

    if opt.buffer:
        f = open(opt.buffer, "r")
        wip = f.read()
    else:
        wip = stdin.read()

    lines = convert_line_numbers(
        create_diff(
            old_content=wip,
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
