from envoy import run as envoy_run
from os.path import relpath

from daemon.scm import SCMBase
from daemon.utils import make_tempfile, cached_property


class Git(SCMBase):
    def _get_commit_cwd(self):
        r = envoy_run('git rev-list HEAD')
        commits = r.std_out.split('\n')
        for commit in commits:
            r = envoy_run('git branch -r --contains %s' % commit)
            if r.std_out != "":
                return commit

        return None

    def _get_original_file(self):
        relative_file_path = relpath(self.file_path, self._repo_path)
        r = envoy_run('git show %s:"%s"' % (self.commit, relative_file_path))
        return make_tempfile(r.std_out)
