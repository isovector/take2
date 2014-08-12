from json import dumps, loads
from re import sub
from sys import stderr, stdout, stdin

from daemon.parser import build_parser
from daemon.scm.diff import create_diff, convert_line_numbers
from daemon.scm.git import Git


OPTIONS = {
    '--old_commit': str,
    '--new_commit': str,
    '--filename': str,
    '--repo_path': str,
}


def translate(args):
    parser = build_parser(OPTIONS)
    opt = parser.parse_args(args=args)

    try:
        git = Git(opt.repo_path)
    except Exception as e:
        stderr.write(str(e))
        return

    line_counts = loads(sub('\s', '', stdin.read()))
    line_counts = dict((int(k), v) for k, v in line_counts.iteritems())

    diff = create_diff(
        old_content=git.get_file_content(opt.filename, opt.old_commit),
        new_content=git.get_file_content(opt.filename, opt.new_commit))
    new_lines = convert_line_numbers(diff, line_counts.keys())

    result = {}

    for i, v in enumerate(line_counts.values()):
        if new_lines[i] is not None:
            result[new_lines[i]] = v

    stdout.write(dumps(result))
    return dumps(result)
