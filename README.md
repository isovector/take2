take2
=====

TODO: write a description of the project

## User Guide

> This section is for users who already have an Accio-enabled project, and just
> need to configure their editor to connect to it.

Running the following command will install the connection protocol on your
computer, and non-destructively update your vim configuration to use the editor
plugin.

```bash
wget https://raw.githubusercontent.com/isovector/take2/master/scripts/install_client.sh && \
chmod +x install_client.sh && \
bash install_client.sh && \
rm install_client.sh
```

If your .vimrc file cannot be patched safely, it will cancel the
installer and direct you with instructions to continue manually.  Contact Sandy
(isovector) if you need additional help.

Setup will ask for root to install the connection protocol. If you don't trust
us, you can install it directly from pip:

```bash
sudo pip install accio
```


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

After committing the newly created files, your project *should* be
Accio-enabled. Direct all of your developers to the **User Guide** section to
get them connected.

