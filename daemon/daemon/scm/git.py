from envoy import run as envoy_run

from daemon.scm import SCMBase


class Git(SCMBase):
    def _get_commit_cwd(self):
        r = envoy_run('git rev-parse origin/HEAD')
        return r.std_out
