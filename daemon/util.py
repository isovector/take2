from os import unlink
from tempfile import mkstemp

def make_tempfile(content=None):
    """ Creates a temporary file and returns the path. """
    fd, tmpfile = mkstemp()

    if content:
        os.write(fd, content)

    os.close()
    return tmpfile

def delete_tempfile(path):
    """ Deletes a temporary file """
    try:
        unlink(path)
    except:
        pass
