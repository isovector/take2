from argparse import ArgumentParser
from os.path import commonprefix
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

    server_urls = repo_url(opt.filename)

    if server_urls is None:
        return

    git = Git()
    print git.get_commit(opt.filename)

    current_file = make_tempfile(stdin.read())

# DO STUFF

    delete_tempfile(current_file)

def repo_url(path):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for k, v in REPOSITORY_PATHS.iteritems():
        if path.startswith(k):
            return v

    return None
