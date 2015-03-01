from json import dumps, loads
from re import sub
from sys import stderr, stdout, stdin
from os.path import relpath

from accio.scm.diff import create_diff, convert_line_numbers
from accio.scm.git import Git


def translate(old_commit, new_commit, filename, repo_path, inp):
    filename = relpath(filename, repo_path)
    repo_path = relpath(repo_path)

    try:
        git = Git(repo_path)
    except Exception as e:
        stderr.write(str(e))
        return

    # Load the line numbers and the times they have been looked at
    line_counts = loads(sub('\s', '', inp))
    # Put those line numbers in a dictionary so we can translate them to the new diff
    line_counts = dict((int(k), v) for k, v in line_counts.iteritems())

    diff = create_diff(
        old_content=git.get_file_content(filename, old_commit),
        new_content=git.get_file_content(filename, new_commit))
    new_lines = convert_line_numbers(diff, line_counts.keys())

    result = {}

    for i, v in enumerate(line_counts.values()):
        if new_lines[i] is not None:
            result[new_lines[i]] = v

    return dumps(result)
