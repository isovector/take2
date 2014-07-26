from abc import ABCMeta, abstractmethod
from os import chdir, getcwd
from os.path import dirname


class SCMBase(object):
    __metaclass__ = ABCMeta

    def get_commit(self, path):
        """ Retrieve commit information given a path to a file in the repo """
        original_path = getcwd()
        chdir(dirname(path))

        commit_id = self._get_commit_cwd()

        chdir(original_path)
        return commit_id

    @abstractmethod
    def _get_commit_cwd(self):
        """
        Retrieve commit id of the most recent commit pushed to remote from
        the current directory
        """
        return
