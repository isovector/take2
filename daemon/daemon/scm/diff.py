from daemon.utils import norm_path
from envoy import run as envoy_run

NL="%c'\012'"


def create_diff(new, old):
    """ Returns the diff between the new and old file """
    r = envoy_run(("diff "
        "--unchanged-line-format=\"-%s\" "
        "--old-line-format=\"<%s\" "
        "--new-line-format=\">%s\" "
        "%s %s") % (NL, NL, NL, norm_path(new), norm_path(old))
    )
    return r.std_out.replace('%c', '\n').rstrip("\n").split("\n")


# get line numbers in second arg to diff, given a range in first arg
def convert_line_numbers(diff, start, end):
    left_line = 0
    right_line = 0
    lines = []
    for line in diff:
        if line == '<':
            left_line += 1
        elif line == '>':
            right_line += 1
        else:
            left_line += 1
            right_line += 1
            if start <= left_line and left_line <= end:
                lines.append(right_line)

        if left_line > end:
            break

    return lines
