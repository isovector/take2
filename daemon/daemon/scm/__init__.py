from abc import ABCMeta, abstractmethod
from envoy import run as envoy_run
from os import chdir, getcwd
from os.path import dirname

from daemon.utils import cached_property

NEW_LINE="%c'\012'"

class SCMBase(object):
    __metaclass__ = ABCMeta
    def __init__(self, file_path, repo_path):
        self.file_path = file_path
        self._repo_path = repo_path

    @cached_property
    def commit(self):
        """ Retrieve commit information given a path to a file in the repo """
        original_path = getcwd()
        chdir(dirname(self.file_path))

        commit_id = self._get_commit_cwd()

        chdir(original_path)
        return commit_id

    @abstractmethod
    def _get_commit_cwd(self):
        """
        Retrieve commit id of the most recent commit pushed to remote from
        the current directory.
        """
        return

    @cached_property
    def original_file(self):
        """
        Returns the path to a temporary file containing the contents of the
        specified file at the base commit. The same path should be returned
        every time.
        """
        original_path = getcwd()
        chdir(dirname(self.file_path))

        path = self._get_original_file()

        chdir(original_path)
        return path

    @abstractmethod
    def _get_original_file(self):
        """
        Returns the path to a temporary file containing the contents of the
        specified file at the base commit. The same path should be returned
        every time.
        """
        return

    def diff(self, new, old=None):
        """ Returns the diff between the new and old file """
        old = old or self.original_file
        r = envoy_run(
            ("diff "
            "--unchanged-line-format=\"%s\" "
            "--old-line-format=\"<%s\" "
            "--new-line-format=\">%s\" "
            "%s %s") % (NEW_LINE, NEW_LINE, NEW_LINE, new, old))
        return r.std_out.split("\n")

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
