from os import unlink, write, close
from tempfile import mkstemp

def make_tempfile(content=None):
    """ Creates a temporary file and returns the path. """
    fd, tmpfile = mkstemp()

    if content:
        write(fd, content)

    close(fd)
    return tmpfile

def delete_tempfile(path):
    """ Deletes a temporary file """
    try:
        unlink(path)
    except:
        pass
