from abc import ABCMeta, abstractmethod
from envoy import run as envoy_run
from os import chdir, getcwd
from os.path import dirname

from daemon.utils import cached_property


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
        print "HUH"
        original_path = getcwd()
        chdir(dirname(self.file_path))

        path = self._get_original_file()

        chdir(original_path)
        print path
        print self.commit
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
        r = envoy_run("diff -y %s %s" % (new, old))
        return r.std_out.split("\n")

    # get line numbers in second arg to diff, given a range in first arg
    def apply_diff(self, diff, start, end):
        leftLine = 0
        rightLine = 0
        lines = []
        for line in diff:
            bits = line.split("\t", 1)

            left = bits[0]
            # strip out spacing
            print "=" + line + "="
            print "-" + bits[1] + "-"
            right = bits[1][6:]

            isMod = right[0] != "\t"
            publish = False
            if not isMod:
                # if not a mod, move both pointers
                leftLine += 1
                rightLine += 1

                # output if in the correct range
                if start <= leftLine and leftLine <= end:
                    lines.append(rightLine)
                    print rightLine
            else:
                # otherwise only move one pointer
                right = right[6:]
                if right[0] == "<":
                    leftLine += 1
                else:
                    rightLine += 1

        return lines
