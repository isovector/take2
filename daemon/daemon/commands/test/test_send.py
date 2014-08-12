from git import Repo
from envoy import run as envoy_run
from json import loads
from os.path import join, normpath
from unittest2 import TestCase

from daemon.utils import delete_tempfolder, make_tempdir, norm_path


class SendTest(TestCase):
    def test_send(self):
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

        result = envoy_run(
            ('accio send --filename %s --start 2 --end 5 --debug ' +
                '--repo_path %s --server_url %s')
            % (norm_path(filename), norm_path(folder), "test"),
            data="1\n2\n3\n4\n5\n6")

        self.assertNotEqual(result.std_out, '')
        payload = loads(result.std_out)
        self.assertEqual(payload['lines[]'], [2, 5, 6])

        delete_tempfolder(folder)
        delete_tempfolder(remote)
