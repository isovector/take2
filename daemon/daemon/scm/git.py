from envoy import run as envoy_run

from daemon.scm import SCMBase
from daemon.utils import make_tempfile, cached_property, norm_path

import os


class Git(SCMBase):
    def _get_commit(self):
        r = envoy_run('git rev-list HEAD')
        commits = r.std_out.split('\n')
        for commit in commits:
            r = envoy_run('git branch -r --contains %s' % commit)
            if r.std_out != "":
                return commit

        return None

    def _get_original_file(self):
        r = envoy_run('git show %s:"%s"' % (
            self.commit,
            norm_path(self.relative_file_path)))

        return make_tempfile(r.std_out)

    def _get_email(self):
        r = envoy_run('git config user.email')
        return r.std_out
