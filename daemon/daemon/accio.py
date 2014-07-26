from argparse import ArgumentParser

from daemon import DAEMON_MAIN

def main():
    parser = ArgumentParser(
        prog=DAEMON_MAIN,
        usage='%(prog)s [<args>]',
        add_help=False)

    parser.add_argument('--filename',  type=str)
    parser.add_argument('--start',  type=int)
    parser.add_argument('--end',  type=int)
    opt = parser.parse_args()


