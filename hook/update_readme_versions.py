#!/usr/bin/env python3
# -*- coding: utf-8 -*-
#
# MIT License
#
# Copyright (c) 2018 Jianshen Liu
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
"""Update the versions in example scripts in the README.md file."""

import os
import re
import sys


def main():
    basedir = os.path.dirname(os.path.realpath(__file__)) + '/../'

    new_version = sys.argv[1]
    version_suffix = r"-SNAPSHOT" if "-SNAPSHOT" in new_version else ""

    readme_file = os.path.join(basedir, 'README.md')

    with open(readme_file, 'r') as file_obj:
        content = file_obj.read()

    old_version = re.search(
        r"rocksdbjni\-rmi\-([\d.]+)" + version_suffix + r"\.jar",
        content).group(1)

    content = content.replace(old_version + version_suffix, new_version)

    # write back to file
    with open(readme_file, 'w') as file_obj:
        file_obj.write(content)

    # maven-release-plugin will first pass in a version string without
    # "-SNAPSHOT", then pass in a version with with "-SNAPSHOT". So we commit
    # the changes after the second invocation of this script.
    if version_suffix:
        os.system('git add ' + readme_file)  # nosec
        os.system(  # nosec
            'git commit -m ' +
            '"[maven-release-plugin] update version strings in README"')
        print("Committed changes of version strings in README")


if __name__ == "__main__":
    main()
