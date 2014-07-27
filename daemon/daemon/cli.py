from argparse import ArgumentParser
from sys import stdin

from daemon import DAEMON_MAIN
from daemon.config import REPOSITORY_PATHS
from daemon.scm.git import Git
from daemon.utils import make_tempfile, delete_tempfile


def main():
    parser = ArgumentParser(
        prog=DAEMON_MAIN,
        usage='%(prog)s [<args>]',
        add_help=False)

    parser.add_argument('--filename',  type=str)
    parser.add_argument('--start',  type=int)
    parser.add_argument('--end',  type=int)
    opt = parser.parse_args()

    repo_path, server_urls = repo_url(opt.filename)

    if server_urls is None:
        return

    git = Git(opt.filename, repo_path)
    commit_id = git.commit

    current_file = make_tempfile(stdin.read())

    print current_file

    diff = git.diff(new=current_file)
    lines = git.apply_diff(diff=diff, start=opt.start, end=opt.end)

    #delete_tempfile(current_file)
    #delete_tempfile(git.original_file)

    print lines

def repo_url(path):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for k, v in REPOSITORY_PATHS.iteritems():
        if path.startswith(k):
            return k, v

    return None
