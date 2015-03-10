from envoy import run as envoy_run

from accio.scm import SCMBase


class Git(SCMBase):
    def _get_last_pushed_commit(self):
        r = envoy_run('git rev-list HEAD')
        commits = r.std_out.split('\n')
        for commit in commits:
            r = envoy_run('git branch -r --contains %s' % commit)
            if r.std_out != "":
                return commit

        return None

    def _get_file_content(self, file_path, commit):
        r = envoy_run('git show %s:"%s"' % (commit,file_path.replace('\\', '/')))
        return r.std_out

    def _get_name(self):
        r = envoy_run('git config user.name')
        return r.std_out.strip()

    def _get_email(self):
        r = envoy_run('git config user.email')
        return r.std_out.strip()

    def _get_branch(self):
        r = envoy_run('git rev-parse --abbrev-ref HEAD')
        return r.std_out.strip()
