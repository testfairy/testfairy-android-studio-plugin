#!/bin/bash

set -e

ideaVersion="2021.3"

if [ ! -d ./idea-ic ]; then
    # Get our IDEA dependency
    if [ -f ~/Tools/ideaIC-${ideaVersion}.tar.gz ];
    then
        cp ~/Tools/ideaIC-${ideaVersion}.tar.gz .
    else
        curl -OL https://download.jetbrains.com/idea/ideaIC-${ideaVersion}.tar.gz
#        curl -OL http://download.labs.intellij.net/idea/ideaIC-${ideaVersion}.tar.gz
    fi

    # Unzip IDEA
    tar zxf ideaIC-${ideaVersion}.tar.gz
    rm -rf ideaIC-${ideaVersion}.tar.gz

    # Move the versioned IDEA folder to a known location
    ideaPath=$(find . -name 'idea-IC*' | head -n 1)
    mv ${ideaPath} ./idea-ic
fi
