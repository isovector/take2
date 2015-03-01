from git import Repo
from envoy import run as envoy_run
from json import loads, dumps
from os.path import join
from unittest2 import TestCase
from accio.commands.translate import translate

from accio.utils import (
    delete_tempfolder,
    make_tempdir,
    norm_path,
    make_tempfile)


class TranslateTest(TestCase):
    def test_translate(self):
        folder = make_tempdir()
        filename = 'test.txt'
        full_filename = join(folder, filename)

        repo = Repo.init(folder)

        f = open(full_filename, 'w')
        f.write("1\n2\n7\n5\n6")
        f.close()

        repo.git.add('.')
        repo.git.commit(m="testing")

        f = open(full_filename, 'w')
        f.write("1\n2\n3\n4\n5\n6")
        f.close()

        repo.git.add('.')
        repo.git.commit(m="testing2")

        data = {
            1: 1,
            2: 4,
            3: 5,
            5: 7,
        }

        expected = {
            "1": 1,
            "2": 4,
            "6": 7,
        }

        json = translate(
            "HEAD~",
            "HEAD",
            norm_path(filename),
            norm_path(folder),
            dumps(data)
        )

        payload = loads(json)
        self.assertEqual(payload, expected)

        delete_tempfolder(folder)
