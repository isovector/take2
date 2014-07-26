from envoy import run as envoy_run
from os.path import relpath

from daemon.scm import SCMBase
from daemon.utils import make_tempfile, cached_property


class Git(SCMBase):
    def _get_commit_cwd(self):
        r = envoy_run('git rev-parse origin/HEAD')
        return r.std_out

    def _get_original_file(self):
        relative_file_path = relpath(self.file_path, self._repo_path)
        print 'git show %s:"%s"' % (self.commit, relative_file_path)
        r = envoy_run('git show %s:"%s"' % (self.commit, relative_file_path))
        print "-----------------------"
        print r.std_out
        print "-----------------------"
        return make_tempfile(r.std_out)
