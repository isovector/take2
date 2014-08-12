from abc import ABCMeta, abstractmethod
from os import chdir, getcwd
from os.path import relpath


class SCMBase(object):
    __metaclass__ = ABCMeta

    def __init__(self, repo_path):
        self.__repo_path = repo_path

        original_path = getcwd()
        chdir(self.__repo_path)

        self.name = self._get_name()
        self.email = self._get_email()
        self.branch = self._get_branch()

        chdir(original_path)

        if self.email == "":
            raise Exception('Snapshot not sent: No email has been provided')

    def relative_file_path(self, file_path):
        """ Returns the filepath relative to the repository """
        return relpath(file_path, self.__repo_path)

    @abstractmethod
    def _get_name(self):
        """ Gets the name associated with the SCM """
        return

    @abstractmethod
    def _get_email(self):
        """ Gets the email associated with the SCM """
        return

    @abstractmethod
    def _get_branch(self):
        """ Gets the current branch of the SCM """
        return

    def get_last_pushed_commit(self):
        """
        Retrieve commit id of the most recent commit pushed to remote from
        the current directory.
        """
        original_path = getcwd()
        chdir(self.__repo_path)
        result = self._get_last_pushed_commit()
        chdir(original_path)

        return result

    @abstractmethod
    def _get_last_pushed_commit(self):
        return

    def get_file_content(self, file_path, commit):
        """
        Returns the path to a temporary file containing the contents of the
        specified file at the base commit. The same path should be returned
        every time.
        """
        original_path = getcwd()
        chdir(self.__repo_path)
        result = self._get_file_content(file_path, commit)
        chdir(original_path)

        return result

    @abstractmethod
    def _get_file_content(self, file_path, commit):
        return
