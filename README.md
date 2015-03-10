Accio
=====

Accio is a extended analytics platform for collaborative coding environments.
Its main features track *who's been working on what* and *who knows what* about
your codebase. Additionally, it uses real-time sampling of your programming
habits to get a sense of where your codebase might be smelly; if two files
always need to be edited at the same time, there is likely an implicit
dependency to be found here.

In short, Accio gives you a high-level view about the management aspects of your
project.


## User Guide

> This section is for users who already have an Accio-enabled project, and just
> need to configure their editor to connect to it.

Most of Accio's benefits come from tracking what you're working on, while you
work on it. This functionality comes in the form of a vim (other editors to be
added one day) plugin, which will periodically inform the Accio server about
what you're working on, and how it's diverged from the last pushed commit. The
purpose of this section is to help get this plugin set up.

Running the following command will install the connection protocol on your
computer, and non-destructively update your vim configuration to use the editor
plugin.

```bash
wget -O-
https://raw.githubusercontent.com/isovector/take2/master/scripts/install_client.sh | bash
```

If your .vimrc file cannot be patched safely, it will cancel the installer and
direct you with instructions to continue manually.  Contact Sandy (isovector) if
you need additional help.

Setup will ask for root to install the connection protocol. If you don't trust
us, you can first install it directly from pip (but you will still need to run
the above script):

```bash
sudo pip install accio
```

You should now be good to go! Whenever you are editing a file in a project that
is Accio-enabled, your vim will silently send reports of what you're working on
to the Accio server, which will use this information to perform implicit code
coupling analytics on the codebase. Sweet!

### Extra steps for Sublime Text

Copy the file `take2/front/sublime/accio.py` into your plugins folder. For windows
this can generally be found in
`C:\Users\__user_name__\AppData\Roaming\Sublime Text 2\Packages\User\'
and for linux based systems, this is generally found in
`~/Library/Application Support/Sublime Text 2/Packages/User`.
If the location doesn't exist, you can check in Sublime by clicking
Preferences > Browse Packages... menu


## Maintainer Guide

> This section is for project maintainers who want to enable Accio support for
> their project.

Contact Sandy (isovector) or Matt (MatthewMaclean) to get a unique project
number for your hosting account. Once this has been granted to you, in the root
of your project:

```bash
wget
https://raw.githubusercontent.com/isovector/take2/master/scripts/init_project.sh && \
chmod +x init_project.sh && \
bash init_project.sh && \
rm init_project.sh
```

Add a user named **AccioServer** as a contributor with read-access on your
*Github*- or *Bitbucket*-hosted repository. This will allow Accio to poll for
code changes and keep our analytics up to date.

Your project should now be Accio-enabled. Direct all of your developers to the
**User Guide** section to get them connected.
