from os import unlink, write, close
from shutil import rmtree
from tempfile import mkstemp, mkdtemp


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


def delete_tempfolder(path):
    """Deletes a temporary folder """
    try:
        rmtree(path)
    except:
        pass


def norm_path(path):
    return path.replace('\\', '/')


def make_tempdir():
    """Creates a temporary folder directory and returns the path."""
    return mkdtemp()


class cached_property(object):
    """ Taken from Django

    Decorator that converts a method with a single self argument into a
    property cached on the instance.

    Optional ``name`` argument allows you to make cached properties of other
    methods. (e.g.  url = cached_property(get_absolute_url, name='url') )
    """
    def __init__(self, func, name=None):
        self.func = func
        self.name = name or func.__name__

    def __get__(self, instance, type=None):
        if instance is None:
            return self
        res = instance.__dict__[self.name] = self.func(instance)
        return res
