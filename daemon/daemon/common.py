import os
from os import path


def walk_up(bottom):
    """
    mimic os.walk, but walk 'up'
    instead of down the directory tree
    from: https://gist.github.com/zdavkeos/1098474
    """

    bottom = path.realpath(bottom)

    # get files in current dir
    try:
        names = os.listdir(bottom)
    except Exception as e:
        print e
        return

    dirs, nondirs = [], []
    for name in names:
        if path.isdir(path.join(bottom, name)):
            dirs.append(name)
        else:
            nondirs.append(name)

    yield bottom, dirs, nondirs

    new_path = path.realpath(path.join(bottom, '..'))

    # see if we are att the top
    if new_path == bottom:
        return

    for x in walk_up(new_path):
        yield x


def repo_url(file):
    """
    Returns the list of server urls associated with a file path. If the path
    is not in a supported repository, None is returned.
    """
    for dircontents in walk_up(path.dirname(file)):
        for filename in dircontents[2]:
            dirname = dircontents[0]
            if filename == ".take2rc":
                with open(dirname + os.sep + filename) as f:
                    return dirname, f.read(), path.relpath(file, dirname)
    return None, None, None
