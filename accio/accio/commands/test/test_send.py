from git import Repo
from envoy import run as envoy_run
from json import loads
from os.path import join, normpath
from unittest2 import TestCase

from accio.commands.send import send
from accio.utils import (
    delete_tempfolder,
    make_tempdir,
    norm_path,
    make_tempfile)


class SendTest(TestCase):
    def test_send_file(self):
        folder = make_tempdir()
        remote = make_tempdir()
        filename = join(folder, 'test.txt')

        repo = Repo.init(folder)
        Repo.init(remote, bare=True)

        f = open(filename, 'w')
        f.write("1\n2\n7\n5\n6")
        f.close()

        repo.git.add('.')
        repo.git.commit(m="testing")
        repo.git.remote('add', 'origin', normpath(remote))
        repo.git.push('-u', 'origin', 'master')

        fin = make_tempfile("1\n2\n3\n4\n5\n6")

        json = send(
            norm_path(filename),
            2,
            5,
            norm_path(fin),
            True,
            norm_path(folder)
        )

        self.assertNotEqual(json, False)
        payload = loads(json)
        self.assertEqual(payload['lines[]'], [2, 4])

        delete_tempfolder(folder)
        delete_tempfolder(remote)
