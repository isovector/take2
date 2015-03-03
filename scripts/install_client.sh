#!/usr/bin/env bash

echo -n "checking for pathogen... "
if [ ! -e ~/.vim/bundle ]; then
    echo "missing"
    mkdir -p ~/.vim/autoload ~/.vim/bundle
    curl -LSso ~/.vim/autoload/pathogen.vim https://tpo.pe/pathogen.vim
else
    echo "found"
fi

get_take2() {
    svn checkout https://github.com/isovector/take2/trunk/front/vim &> /dev/null
    mv vim take2
}

echo -n "checking for take2... "
if [ ! -e ~/.vim/bundle/take2 ]; then
    echo "missing"
    cd ~/.vim/bundle
    get_take2
else
    echo "found"
fi

echo -n "checking for accio... "
if hash accio 2>/dev/null; then
    echo "found"
else
    echo "missing"
    echo
    echo "need root to install accio..."
    sudo pip install accio
fi

echo -n "checking for vimrc... "
if [ -e ~/.vimrc ]; then
    if grep "Plug" ~/.vimrc > /dev/null || grep "Bundle" ~/.vimrc > /dev/null; then
        echo "incompatible"
        echo
        echo "FATAL ERROR: incompatible package manager"
        echo "You will need to install the vim plugin manually."
        echo "Please move ./take2/ into your vim package manager."
        get_take2
        echo
        echo "install client failed"
        exit 1
    elif grep "pathogen" ~/.vimrc > /dev/null; then
        echo "pathogen found"
    else
        echo "pathogen missing"
        echo "execute pathogen#infect()" >> ~/.vimrc
    fi
else
    echo "missing"
    cat << EOF > ~/.vimrc
execute pathogen#infect()
syntax on
filetype plugin indent on
EOF
fi

echo
echo "install client succeeded!"
