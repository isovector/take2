from difflib import ndiff

# we might want to integrate this for patience diffing
# http://bazaar.launchpad.net/~bzr-pqm/bzr/bzr.dev/view/head:/bzrlib/_patiencediff_py.py


def create_diff(new_content, old_content):
    """ Returns the diff between the new and old file """
    diff = ndiff(
        a=old_content.splitlines(),
        b=new_content.splitlines(),
        linejunk=None,
        charjunk=None)

    delta = []
    for line in diff:
        if line.startswith('  '):
            delta.append('-')
        elif line.startswith('+ '):
            delta.append('>')
        elif line.startswith('- '):
            delta.append('<')

    return delta


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

    # Ensure that our array sizes are the same if we ran out of lines in diff
    # without matching all of the lines
    for i in range(len(lines)):
        result.append(None)

    return result
