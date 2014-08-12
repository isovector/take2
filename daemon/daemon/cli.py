from daemon.parser import build_parser
from daemon.commands.translate import translate
from daemon.commands.send import send


OPTIONS = {
    'command': str,
}


COMMANDS = {
    'translate': translate,
    'send': send,
}


def main():
    parser = build_parser(OPTIONS)

    (opt, args) = parser.parse_known_args()

    if opt.command not in COMMANDS:
        return

    return COMMANDS[opt.command](args)
