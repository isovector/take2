from abc import ABCMeta, abstractmethod
from envoy import run as envoy_run
from os import chdir, getcwd
from os.path import dirname, relpath

from daemon.utils import cached_property, norm_path

NL="%c'\012'"

class SCMBase(object):
    __metaclass__ = ABCMeta
    def __init__(self, file_path, repo_path):
        self.__file_path = file_path
        self.__repo_path = repo_path

        original_path = getcwd()
        chdir(dirname(self.__file_path))

        self.email = self._get_email()
        self.commit = self._get_commit()
        self.original_file = self._get_original_file()

        chdir(original_path)

        if self.email == "":
            raise Exception('Snapshot not sent: No email has been provided')


    def diff(self, new, old=None):
        """ Returns the diff between the new and old file """
        old = old or self.original_file
        r = envoy_run(("diff "
            "--unchanged-line-format=\"%s\" "
            "--old-line-format=\"<%s\" "
            "--new-line-format=\">%s\" "
            "%s %s") % (NL, NL, NL, norm_path(new), norm_path(old))
        )
        return r.std_out.replace('%c', '\n').split("\n")

    # get line numbers in second arg to diff, given a range in first arg
    def apply_diff(self, diff, start, end):
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

    @cached_property
    def relative_file_path(self):
        """ Returns the filepath relative to the repository """
        return relpath(self.__file_path, self.__repo_path)

    @abstractmethod
    def _get_email(self):
        r = envoy_run('git config user.email' % (
            self.commit,
            self.relative_file_path))

    @abstractmethod
    def _get_commit(self):
        """
        Retrieve commit id of the most recent commit pushed to remote from
        the current directory.
        """
        return

    @abstractmethod
    def _get_original_file(self):
        """
        Returns the path to a temporary file containing the contents of the
        specified file at the base commit. The same path should be returned
        every time.
        """
        return
