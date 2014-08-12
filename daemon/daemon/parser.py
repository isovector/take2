from argparse import ArgumentParser

from daemon import DAEMON_MAIN


def build_parser(options):
    parser = ArgumentParser(
        prog=DAEMON_MAIN,
        usage='%(prog)s <command> [<args>]',
        add_help=False)

    for k, v in options.iteritems():
        if (v == bool):
            parser.add_argument(k, action="store_true")
        else:
            parser.add_argument(k, type=v)

    return parser
