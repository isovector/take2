from os import getcwd
from os.path import join
from unittest2 import TestCase
from daemon.utils import make_tempfile, delete_tempfile

from daemon.scm import SCMBase


class FakeSCM(SCMBase):
    def __init__(self, file_path, repo_path):
        pass

    def _get_commit(self):
        pass

    def _get_original_file(self):
        pass

    def _get_name(self):
        pass

    def _get_email(self):
        pass

class SCMTest(TestCase):
    def setUp(self):
        self.scm = FakeSCM("", "")

    def test_diff_additions(self):
        diff = ['', '', '>', '>', '', '']
        self.assertEqual(self.scm.apply_diff(diff, 1, 3), [1, 2, 5])

    def test_diff_subtractions(self):
        diff = ['', '', '<', '', '<', '', '']
        self.assertEqual(self.scm.apply_diff(diff, 2, 6), [2, 3, 4])

    def test_diff_modifications(self):
        diff = ['-', '-', '<', '>', '-', '-']
        self.assertEqual(self.scm.apply_diff(diff, 1, 5), [1, 2, 4, 5])

        diff = ['-', '-', '<', '<', '>', '-', '-']
        self.assertEqual(self.scm.apply_diff(diff, 2, 5), [2, 4])

        diff = ['-', '-', '<', '<', '>', '>', '>', '-', '-', '-', '-']
        self.assertEqual(self.scm.apply_diff(diff, 5, 8), [6, 7, 8, 9])

    def test_create_diff(self):
        a = make_tempfile("a\nb\nc\n")
        b = make_tempfile("a\nc\n")
        self.assertEqual(self.scm.create_diff(new=a, old=b), ['-', '<', '-'])
        delete_tempfile(a);
        delete_tempfile(b);

        a = make_tempfile("a\nc\n")
        b = make_tempfile("a\nb\nc\n")
        self.assertEqual(self.scm.create_diff(new=a, old=b), ['-', '>', '-'])
        delete_tempfile(a);
        delete_tempfile(b);
