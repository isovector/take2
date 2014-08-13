from envoy import run as envoy_run
from daemon.utils import make_tempfile, delete_tempfile, norm_path

NL = "%c'\012'"


def create_diff(new_content, old_content):
    """ Returns the diff between the new and old file """
    new_file = make_tempfile(new_content)
    old_file = make_tempfile(old_content)

    r = envoy_run(
        ("diff "
            "--unchanged-line-format=\"-%s\" "
            "--old-line-format=\"<%s\" "
            "--new-line-format=\">%s\" "
            "%s %s") %
        (NL, NL, NL, norm_path(old_file), norm_path(new_file))
    )

    delete_tempfile(new_file)
    delete_tempfile(old_file)
    return r.std_out.replace('%c', '\n').rstrip("\n").split("\n")


def add_to_result(lines, result, old_line, new_line):
    if lines[0] == old_line:
        result.append(new_line)
        lines.pop(0)


# get line numbers in second arg to diff, given a range in first arg
def convert_line_numbers(diff, lines):
    lines.sort()

    left_line = 0
    right_line = 0
    result = []
    for line in diff:
        if line == '<':
            left_line += 1
            add_to_result(lines, result, left_line, None)
        elif line == '>':
            right_line += 1
        else:
            left_line += 1
            right_line += 1
            add_to_result(lines, result, left_line, right_line)

        if not len(lines):
            break

    return result
