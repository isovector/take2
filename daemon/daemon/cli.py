from argparse import ArgumentParser
from sys import stdin

from daemon import DAEMON_MAIN
from daemon.scm.git import Git
from daemon.util import make_tempfile, delete_tempfile

def main():
    parser = ArgumentParser(
        prog=DAEMON_MAIN,
        usage='%(prog)s [<args>]',
        add_help=False)

    parser.add_argument('--filename',  type=str)
    parser.add_argument('--start',  type=int)
    parser.add_argument('--end',  type=int)
    opt = parser.parse_args()

    git = Git()
    print git.get_commit(opt.filename)

    current_file = make_tempfile(stdin.read())

# DO STUFF

    delete_tempfile(current_file)

