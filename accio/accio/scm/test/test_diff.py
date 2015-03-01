from unittest2 import TestCase

from accio.scm.diff import create_diff, convert_line_numbers


class DiffTest(TestCase):
    def test_diff_additions(self):
        self.assertEqual(convert_line_numbers(
            ['-', '-', '>', '>', '-', '-'],
            range(1, 4)),
            [1, 2, 5])

    def test_diff_subtractions(self):
        self.assertEqual(convert_line_numbers(
            ['-', '-', '<', '-', '<', '-', '-'],
            range(2, 7)),
            [2, None, 3, None, 4])

    def test_diff_modifications(self):
        self.assertEqual(convert_line_numbers(
            ['-', '-', '<', '>', '-', '-'],
            range(1, 6)),
            [1, 2, None, 4, 5])

        self.assertEqual(convert_line_numbers(
            ['-', '-', '<', '<', '>', '-', '-'],
            range(2, 6)),
            [2, None, None, 4])

        self.assertEqual(convert_line_numbers(
            ['-', '-', '<', '<', '>', '>', '>', '-', '-', '-', '-'],
            range(5, 9)),
            [6, 7, 8, 9])

    def test_create_diff(self):
        self.assertEqual(
            create_diff(
                new_content="a\nb\nc\n",
                old_content="a\nc\n"),
            ['-', '>', '-'])

        self.assertEqual(
            create_diff(
                new_content="a\nc\n",
                old_content="a\nb\nc\n"),
            ['-', '<', '-'])
