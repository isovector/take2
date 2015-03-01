from os import getcwd
from os.path import join
from pep8 import StyleGuide
from unittest2 import TestCase


class LinterTest(TestCase):
    def test_pep8_conformance(self):
        pep8style = StyleGuide(testsuite=True)
        report = pep8style.init_report()

        pep8style.input_dir(dirname=join(getcwd(), 'accio'))

        self.assertEqual(report.total_errors, 0, "Found code style errors.")
