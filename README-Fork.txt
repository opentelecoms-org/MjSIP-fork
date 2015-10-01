
The MjSIP code was forked to create Sipdroid

A modified version of the MjSIP v1.6 code was imported into
the Sipdroid repository in SVN revision 50
   http://sipdroid.googlecode.com/svn/trunk@50
That is commit 88d6641e39cdf3b8917c80fde2c647b31cd15763 in the Lumicall
Git repository.

That initial commit includes about 30,000 lines of code, 98% of which
is just reformatting.  This has been split into two commits to
create this fork repository, they are:

commit f6689fc2d524c7d3c8ace0c4442c09a862835df8
Author: Pascal Merle <pmerle@bluebottle.com>
Date:   Tue Apr 28 02:22:15 2009 +0200

    Initial import (formatting changes).

commit 9df661c8ae13e5cda34a97cf2c258c16604e755b
Author: Pascal Merle <pmerle@bluebottle.com>
Date:   Tue Apr 28 02:22:16 2009 +0200

    Initial import (various fixes).

To split the commit into two commits, the following strategy was used:

- checked out the commit 88d6641e39cdf3b8917c80fde2c647b31cd15763 in
  the Lumicall repository

- copied the content of the src/org/zoolu tree

- removed three lines from SipProvider.java that reference the class
  Sipdroid:

    sed -i -e 's!^.*Sipdroid!//!g' \
       ${SIPDROID_SRC}/org/zoolu/sip/provider/SipProvider.java

- compile and JAR the original MjSIP 1.6 code using Java 1.5:

    mkdir /tmp/mjsip-1.6-classes
    javac -encoding ISO-8859-1 -g:none -source 1.5 -target 1.5 \
      -d /tmp/mjsip-1.6-classes -sourcepath src \
      `find src/org/zoolu -name '*.java'`
    jar cf /tmp/mjsip-1.6.jar -C /tmp/mjsip-1.6-classes .

- compile and JAR the modified code using Java 1.5:

    mkdir /tmp/mjsip-sipdroid-classes
    javac -encoding ISO-8859-1 -g:none -source 1.5 -target 1.5 \
      -d /tmp/mjsip-sipdroid-classes -sourcepath src \
      `find src/org/zoolu -name '*.java'`
    jar cf /tmp/mjsip-sipdroid.jar -C /tmp/mjsip-sipdroid-classes .

- load both JARs into jd-gui (Java Decompiler tool)

- export each JAR to a source ZIP using "Save All Sources"

- diff the contents of the JARs to show the code changes, these
  were committed as 9df661c8ae13e5cda34a97cf2c258c16604e755b
  with the commit message "Initial import (various fixes)."

- copy the source files from the Sipdroid commit 50 over the top
  of the src/org/zoolu tree, so we get all the whitespace the
  way that it is in Sipdroid, commit f6689fc2d524c7d3c8ace0c4442c09a862835df8
  with log message "Initial import (formatting changes)."

To copy the rest of the commits from the Lumicall repository, they
were extracted with "git format-patch" and applied with
"git am --ignore-space-change"


