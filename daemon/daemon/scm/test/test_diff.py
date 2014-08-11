from unittest2 import TestCase

from daemon.scm.diff import create_diff, convert_line_numbers
from daemon.utils import make_tempfile, delete_tempfile


class DiffTest(TestCase):
    def test_diff_additions(self):
        diff = ['', '', '>', '>', '', '']
        self.assertEqual(convert_line_numbers(diff, 1, 3), [1, 2, 5])

    def test_diff_subtractions(self):
        diff = ['', '', '<', '', '<', '', '']
        self.assertEqual(convert_line_numbers(diff, 2, 6), [2, 3, 4])

    def test_diff_modifications(self):
        diff = ['-', '-', '<', '>', '-', '-']
        self.assertEqual(convert_line_numbers(diff, 1, 5), [1, 2, 4, 5])

        diff = ['-', '-', '<', '<', '>', '-', '-']
        self.assertEqual(convert_line_numbers(diff, 2, 5), [2, 4])

        diff = ['-', '-', '<', '<', '>', '>', '>', '-', '-', '-', '-']
        self.assertEqual(convert_line_numbers(diff, 5, 8), [6, 7, 8, 9])

    def test_create_diff(self):
        a = make_tempfile("a\nb\nc\n")
        b = make_tempfile("a\nc\n")
        self.assertEqual(create_diff(new=a, old=b), ['-', '<', '-'])
        delete_tempfile(a)
        delete_tempfile(b)

        a = make_tempfile("a\nc\n")
        b = make_tempfile("a\nb\nc\n")
        self.assertEqual(create_diff(new=a, old=b), ['-', '>', '-'])
        delete_tempfile(a)
        delete_tempfile(b)
