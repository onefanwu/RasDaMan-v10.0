.. highlight:: bash

.. _sec-contributing:

########################
Contributing to Rasdaman
########################


We'd like to encourage everyone to contribute to the project in whatever ways
they can. This is a volunteer project; all help is greatly appreciated.

We don't have a rigid styleguide or set of rules for contributing to rasdaman;
however, with the goal of making things easier for all developers, here are a
few suggestions:

* **Get feedback from other people**

    This is what the `Mailing Lists <http://rasdaman.org/wiki/MailingLists>`_ and
    this Trac project management system are for. It is always a good idea to talk
    to other devs on the mailing lists before submitting changes.

* **Use Trac tickets**

    This is important to track progress and activity. If you start working on an
    issue, accept the ticket. After you have finished, close the ticket, and add
    information about the changeset in the comment field. Provide also progress
    information when you make relevant changes as described in the
    `UseOfTickets <http://rasdaman.org/wiki/UseOfTickets>`_ page

* **Write tests**

    Please write tests for any new functionality. See `RasdamanTestSuites
    <http://rasdaman.org/wiki/RasdamanTestSuites>`_ for instructions. We ask you
    for your understanding that patches are likely to get rejected if they do
    not contain adequate additions to the *systemtest*.

* **Stick to the Coding Standards**

    The `rasdaman code guide <http://rasdaman.org/wiki/CodeGuide>`_ is mandatory
    for all code. We ask you for your understanding that patches are likely to
    get rejected if they do not adhere to this guide.

* **Use meaningful commit messages and reference tickets**

    such messages help developers to understand what your goal and intent is.
    Further, it eases writing of release notes for new versions. Note that
    patches not starting with "ticket:nnn" will be automatically rejected.


*************************
Development Contributions
*************************

You developed a fix or some new functionality? We are grateful for your
contribution. Of course we like any useful information, but best (and fastest)
for inclusion is to be in sync with our development tools and processes. The
following details are provided to help in this respect.

1. All our development is in Linux. Please consider this for your code.
2. We use *git* as a version management tool, so you may want do do that too.
   Check out from the repository using: ::

    $ git clone git://rasdaman.org/rasdaman.git
    $ git config --global user.name "Name Surname"
    $ git config --global user.email my_email@address.xyz

3. rasdaman should be configured and compiled with `-DENABLE_STRICT=ON` to make
   sure that your patch doesn't introduce new warnings.
4. After ensuring the tests are successful (see TODO systemtest link), stage and
   commit your changes (whereby NNNN indicates the number of the ticket that is
   fixed with this patch): ::

    $ git add <file1> <file2> <dir1/> <dir2>/*.java ...
    $ git commit -m "ticket:NNNN - My brief explanation of the patch"

5. Prepare your patch package through: ::

    $ git format-patch -n

  where ``n`` is the number of last commits that you want to create patch files for.

6. Upload your patch file (or a ``.tar.gz`` archive in case of several files)
   using `Patch Manager <http://rasdaman.org/patchmanager>`_. You will have to accept
   the `Contributor Agreement <http://rasdaman.org/wiki/ContributorAgreement>`_.
   Without your stated consent we unfortunately cannot accept it, due to legal reasons.


*************
Documentation
*************

Any changes to public interfaces likely require updating the rasdaman
documentation. This is a short guide on how to do this.


Getting started
===============

1. Install dependencies ::

    $ sudo pip install -U sphinx sphinx_rtd_theme

2. Main documentation can be found in ``doc/main`` (``*.rst`` files).

3. Build the docs specifically: ::

    $ make doc       # generate all documentation
    $ make doc-html  # generate HTML documentation (requires sphinx)
    $ make doc-pdf   # generate PDF documentation (requires sphinx, latexmk, texlive)
    $ make doc-cpp   # generate C++ API documentation (requires doxygen)

    # alternatively in doc/main/
    $ ./build.sh


Make changes
------------

- Check the short intro below for the reST syntax

  - ... but it should be fairly clear from looking at the docs sources

- Create a review request with `arc diff` before pushing changes.



Quick intro to reStructuredText
===============================

Section headers
---------------

In each case the underline or overline marker should be as long as the section
header (use monospace font to do this correctly). From highest level to most
granular section level:

1. ``#`` - Parts (overline and underline)
2. ``*`` - Chapters (overline and underline)
3. ``=`` - Sections (underline)
4. ``-`` - Subsections (underline)
5. ``^`` - Subsubsections (underline)

Example from the QL guide:

.. code-block:: text

    ####################
    Query Language Guide
    ####################

    ************
    Introduction
    ************

    Multidimensional Data
    =====================

    Subsection
    ----------

    Subsubsection
    ^^^^^^^^^^^^^

Text formatting
---------------

.. code-block:: text

    *Italics*
    **Bold**
    ``Code``

Cannot be nested, may not start/end with whitespace, and has to be
separated from surrounding text with some non-word characters.


Lists
-----

.. code-block:: text

    * Bulleted list
    * Item two

      * Nested list (note it has to have blank line before and after!)

    - Bulleted list continues; you can use - instead of *

    1. Numbered list
    2. Item two

    #. Automatically numbered list
    #. Item two


    term (single line)
        Definition of the term (indented on the next line)

        Definition continues with another paragraph (maintain indentation)


    | Line block
    | line breaks are preserved
    | and appear exactly like this (without the | characters)


`Option lists <http://docutils.sourceforge.net/docs/ref/rst/restructuredtext.html#option-lists>`_
(e.g. the output of ``rasql -h``) can be simply copy pasted, you just need to
make sure the options and their descriptions form two columns.

Source code
-----------

Any source code can go as an indented text after ``::`` (plus blank line).
In the QL guide ``::`` automatically does rasql highlighting. For example:

.. code-block:: text

    ::

        -- example query
        select avg_cells(c) from mr2 as c

renders as

.. code-block:: rasql

    -- example query
    select avg_cells(c) from mr2 as c

For different highlighting you have to use the code-block directive indicating
the language, e.g. java, cpp, xml, javascript, text, ini, etc. Example for java:

.. code-block:: text

    .. code-block:: java

        public static void main(...) {
            ...
        }

You can see all lexers with ``pygmentize -L lexers``; see also
http://pygments.org/languages/


Images
------

If an image has no caption then use the image directive, e.g:

.. code-block:: text

    .. image:: media/logo_full.png
        :align: center
        :scale: 50%


If it has a caption then use the figure directive; the caption is added as an
indented paragraph after a blank line:

.. code-block:: text

    .. _my-label:

    .. figure:: media/logo_full.png
        :align: center
        :scale: 50%

        Caption for the figure.


Hyperlinks
----------

To just have a URL as is nothing special needs to be done, just put as is:

.. code-block:: text

    http://rasdaman.org

To render the URL with alternative text, then the following form should be used:

.. code-block:: text

    `Link text <http://rasdaman.org>`_

Internal cross-referencing can be done by first setting up a label before a
section header or a figure (see above this section Hyperlinks) and then using
it to generate a link anywhere with

.. code-block:: text

    :ref:`my-label`

Instead of :ref: you can use :numref: to get automatic Figure number added to
the link, e.g.

.. code-block:: text

    :numref:`my-label` -> Sec. 2

You can change the default text that :ref: generates like this:

.. code-block:: text

    :ref:`Custom text <my-label>`


Further information
-------------------

- Specification:
  http://docutils.sourceforge.net/docs/ref/rst/restructuredtext.html

- Sphinx guide:
  http://www.sphinx-doc.org/en/master/usage/restructuredtext/index.html



*************
Git resources
*************

- For extensive help on *git* see the `online Git book <http://git-scm.com/book/en>`__.
- For info on git *conflicts* see `Handling and Avoiding Conflicts in Git <http://weblog.masukomi.org/2008/07/12/handling-and-avoiding-conflicts-in-git>`__ or,
  for a quick resolve conflict by discarding any local changes, `this StackOverflow answer <http://stackoverflow.com/questions/101752/aborting-a-merge-in-git/102309#102309>`__.

Further tips:

* `Cleaning local history <http://rasdaman.org/wiki/GitCleanLocalHistory>`_
* `Dealing with rejected patches <http://rasdaman.org/wiki/GitRejectedPatch>`_
* `Git bundles <http://rasdaman.org/wiki/GitCreateBundle>`_
* ...


Basic git for working on tickets
================================

**It is suggested to create a branch in your local working copy of the rasdaman
git repo for each ticket/fix**, so you will not mix up patches. (e.g:
`ticket:1450 <http://rasdaman.org/ticket/1450>`_ -> branch ticket_1450,
`ticket:1451 <http://rasdaman.org/ticket/1451>`_ -> branch ticket_1451, ...)

**Prerequisites**

1. Checkout the newest source code from repository; suppose you did this in
   ``/home/rasdaman/rasdaman`` and you are in this directory in the terminal: ::

    $ pwd
    /home/rasdaman/rasdaman

2. List the branches in your local repository ::

    $ git branch

3. Switch to branch master - as this branch is the canonical
branch for the rasdaman remote repository ::

    $ git checkout master

4. Pull the newest patches if possible from remote repository (rasdaman.org) to your local repository ::

    $ git pull

5. Create a new branch from master branch for a particular fix or feature work: ::

    $ git checkout -b "branch_name" # e.g: git checkout -b "ticket_1451"

    # check current branch, it should be ticket_1451
    $ git branch

**Work and commit changes**

1. You changed some files in the source code directory (e.g: file1.java,
file2.cc,...) and you want to create a commit; first *stage* the changed files: ::

    $ git add file1.java file2.cc ..

.. warning::
  Avoid doing ``git add .``, i.e. adding all changed files automatically.

2. Now you are ready to commit the staged files: ::

    $ git commit -m "ticket:1451 - fix some stuff"

    # see details of your commit on top
    $ git log

3. And create a patch from the commit, i.e. a file with extension ``.patch``
   created from the last commit = ``-1``, which contains all the changes you made: ::

    $ git format-patch -1
    # or for code review
    $ arc diff

3. Finish with this branch by uploading the patch to the `patchmanager
<http://rasdaman.org/patchmanager>`_ and switching to another ticket in a new
branch, starting from master again.


**Switch between pending patches**

E.g you finished one ticket on ticket_1450 and uploaded to the patchmanager
but the patch is rejected and needs to be updated, while you
moved on to working on ticket_1460.

1. First, stage everything you are doing on ticket_1460;
   if you don't want to create a temporary commit, you can just `stash everything in current branch
   <http://gitready.com/beginner/2009/01/10/stashing-your-changes.html>`_. ::

    $ git add <file1> <file2> ...

    # or stash
    $ git stash
    # later can be retrieved with
    $ git stash pop

2. Then commit it as your pending patch on this branch ::

    $ git commit -m "ticket:1460 - fixed stuff"

3. Make sure your current branch is clear ::

    # should report: "nothing to commit, working directory clean"
    $ git status

4. Now switch to your failure patch (e.g: ticket_1450): ::

    $ git checkout ticket_1450

5. Fix the issues here and stage the newly changed files: ::

    $ git add <file 1> <file 2> ...

6. Commit it without changing the ticket's subject: ::

    $ git commit --amend --no-edit

7. Create a patch from the updated commit: ::

    $ git format-patch -1

    # or for code review
    $ arc diff

8. And upload it again to the patchmanager
9. Finally, you can switch back to the previous branch: ::

    $ git checkout ticket_1460


**Apply patches between branches**

E.g you have 1 commit in ticket\_1450 and 1 commit in
ticket\_1460) then you want to add this patch to
ticket\_1460)

1. Check current branch (should be ticket_1450) ::

    $ git branch

2. Create a patch file (like "0001-ticket-1450-fix-some-issues.patch") from the last commit ::

    $ git format-patch -1

3. Switch to other branch ::

    $ git checkout ticket_1460

4. Apply your patch from ticket\_1450 ::

    $ git am -3 0001-ticket-1451-fix-some-issues.patch

5. Check the newest commit (if the patch is applied successfully) ::

    $ git log


**If a patch cannot be applied**

1. You made changes on files which the patch also changes, so you have to merge it manually: ::

    $ git am -3 0001-ticket-1450-fix-some-issues.patch
    # The patch is not applied, some conflict shows here

2. Please follow our `git conflict resolution guide <http://rasdaman.org/wiki/GitRejectedPatch>`_,
   or Steps 3 to 7 of `this resolving merge conflicts guide
   <https://help.github.com/articles/resolving-a-merge-conflict-using-the-command-line/#competing-line-change-merge-conflicts>`_.
3. Once resolved, mark as such: ::

    $ git am --resolved

4. Check that your patch from ticket\_1450 is now the last patch in ticket\_1460 branch: ::

    $ git log


**************
C++ Guidelines
**************

The rasdaman system is implemented in C++ 11; below are some guidelines.


.. _cpp-debugging:

Debugging
=========

The rasdaman code has facilities built in which aid debugging and benchmarking.
On this page information is collected on how to use it. Target audience are
experienced C++ programmers.

.. important::
    It is best to configure rasdaman with ``-DCMAKE_BUILD_TYPE=Debug`` for
    debugging, and ``-DCMAKE_BUILD_TYPE=Release`` for benchmarking (and
    production deployment).

Debuging rasserver
------------------

In *rasnet* (the default network protocol), in order to attach to the ``rasserver``
process (with e.g. ``gdb -p <pid>``) it is necessary to increase the values of
``SERVER_MANAGER_CLEANUP_INTERVAL`` and ``CLIENT_MANAGER_CLEANUP_INTERVAL`` in
``rasmgr_x/src/constants.hh`` to some large values; needless to say this requires
recompiling and restarting rasdaman.

Once that is done, you can attach to a running rasserver process. First find the
process id, second column in the output of ::

    $ ps aux | grep rasserver

It's best to enable only one rasserver in rasmgr.conf or with rascontrol for this
purpose. Then, attach to the pid: ::

    $ gdb -p <pid>

Debugging directql
------------------

When not debugging the network protocol, it's recommended to use ``directql``.
``directql`` has the same interface as ``rasql``, with an important behind the
scenes difference: it is a fully fledged ``rasserver`` itself actually, so
it doesn't need to go through the client protocol. This makes it ideal
for running tools like ``gdb``, ``valgrind``, etc.

When executing directql, use the same parameters as for rasql, but add
``-d /opt/rasdaman/data/RASBASE`` (or substitute that to whatever is the
-connect value in ``rasmgr.conf``).

Example with gdb:

.. code-block:: text

    $ gdb --args directql -q 'query that causes a segfault' \
                          --out file -d /opt/rasdaman/data/RASBASE
    ...
    > run
    ...
    # show a backtrace once the segfault has happened
    > bt


Memory debugging with valgrind
------------------------------

Valgrind can be used to detect uninitialized values, memory errors, and
memory leaks, e.g. ::

    $ valgrind --leak-check=full --track-origins=yes \
               directql -q 'query that causes memory problems' \
                        --out file -d /opt/rasdaman/data/RASBASE


Memory debugging with AddressSanitizer
--------------------------------------

`AddressSanitizer <https://github.com/google/sanitizers/wiki/AddressSanitizer>`_
can be enabled during compilation with ``-DENABLE_ASAN=ON``. This adds
``-fsanitize=address`` to the compiler flags. Please visit the ASAN page for
more details.


Enabling extra output at compile time
-------------------------------------

In order to effect any extra output (besides standard logging) at all,
the code must be compiled with the resp. option enabled. This is not
default in production operation for at least two reasons: writing an
abundance of lines into log files slows down performance somewhat, and,
additionally, logging has a tendency to flood file systems; however, the
option is available when needed.

If you are compiling with cmake, simply use ``-DENABLE_DEBUG=ON``
before doing make. Doing this includes the above cmake
flags for debugging, and it also sets two other variables to enable
more-verbose logging. E.g. in your build directory ::

    $ cmake .. -DCMAKE_INSTALL_PREFIX=$RMANHOME -DCMAKE_BUILD_TYPE=Debug -DENABLE_DEBUG=ON ...
    $ make
    $ make install

You may, optionally, alter settings in $RMANHOME/etc/log-client.conf and
$RMANHOME/etc/log-server.conf to enable various other logging
parameters, e.g. DEBUG and TRACE for extra verbose output in the logs.


Internal array representation
=============================

Internally in rasdaman, multidimensional arrays are handled as a 1-D
array, linearized in `row-major
format <https://en.wikipedia.org/wiki/Row-_and_column-major_order>`__.
Row-major refers to matrices with rows and columns, indicating that
first all cells of the first row are listed in order, then all cells of
the second row, etc. Given that we are working with multidimensional
arrays here, this notion needs to be
`generalized <https://en.wikipedia.org/wiki/Row-_and_column-major_order#Address_calculation_in_general>`__:
the inner-most (last) axis is contiguous, and varies fastest, followed
by the second last axis and so on.

For example, let's say we have an array with sdom ``[5:10, -2:2, 0:5]``.
The 1-D internal\_array (in code) corresponds to external\_array (in rasql):

.. code-block:: cpp

    linear_index := 0
    for i := 5..10
      for j := -2..2
        for k := 0..5
          internal_array[linear_index] == external_array[i, j, k]
          linear_index += 1



************
Adding Tests
************

**TODO**: this is somewhat outdated and incomplete.

The rasdaman source tree comes with integration tests ("systemtest" for
historical reasons) and unit tests (in each component directory ``X`` there is a
subdirectory ``X/test/``). To run the integration test: ::

        $ cd systemtest
        $ make check

After your patch submission, the patchmanager will automatically run the
systemtest in a sandbox; the result will be flagged in the patchmanager table
for each patch submitted. Allow some time (usually 1.5 hours) until the result
gets visible. Patches which do not pass systemtest will be rejected without
further inspection.

``make check`` will automatically find all tests in the four test case
directories, specifically, testcases\_mandatory, testcases\_petascope,
testcases\_fixed and testcases\_open.

1. whenever a bug is found, a corresponding test should be created in the testcases\_open directory;
2. when the bug is fixed, the test should be moved to the testcases\_fixed directory;
3. testcases\_services holds the test cases for petascope and secore;
4. testcases\_mandatory holds the test cases for rasql typically.

Each test should have a folder which is inside one of the above mentioned
directories, by convention named ``test_X``, e.g. ``test_select``. The test
should be executed by a shell script inside the folder; its exit code indicates
whether the test passed (0) or failed (non-0). Details of the test execution
should be logged in the same folder. In ``systemtest/util`` there are various
bash utility functions that can be used in the test scripts, e.g. for logging,
checking result, etc.

Add a rasql test query
======================

1. save the test query as ``systemtest/test_mandatory/test_select/queries/<queryName>.rasql``
2. save the expected query result file in ``systemtest/test_mandatory/test_select/oracle/<queryName>.oracle``

To generate a test oracle:

1. if the result is a scalar, run ::

        rasql -q  "<query>" --out string | grep Result > <queryName>.oracle

2. if the result is an array, run ::

        rasql -q  "<query>" --out file --outfile <queryName>.oracle

Make sure to validate the correctness of the oracle before adding
to the systemtest.

If a query is *known to fail* and documented by a ticket, it can be marked
in the systemtest, so that the result of that query is *SKIPPED*, rather
than *FAILED*. To do this create a file ``known_fails`` (if not yet
existing) in the corresponding test dir (next to the ``test.sh``) and
put each query file name in a single line in this file.

Add a petascope test
====================

The scripts for WMS, WCS and WCPS testing can be found respectively in:

* ``rasdaman/systemtest/testcases_services/test_wcps``
* ``rasdaman/systemtest/testcases_services/test_wcs``
* ``rasdaman/systemtest/testcases_services/test_wms``

To run a specific test (besides ``make check`` that runs the whole systemtest),
go to the directory and execute ::

        $ ./test.sh

Do **not** execute ``sh test.sh`` as the script is written for bash, and ``sh``
is often linked to a restricted version of bash like dash, or similar.
Variables like Tomcat port, host, ``rasdaman`` connection details, etc. may need
to be adapted before running the tests by editing ``rasdaman/systemtest/conf/test.cfg``.

Testdata
--------

The following coverages are available for the tests (see ``rasdaman/systemtest/testcases_services/test_wcps/README``):

+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| coverage       | dim | type  | pixel extent    |    axes    | geo-boundingbox |  time extension |   CRS     |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``rgb``        | 2D  | rgb   | 0:399,0:343     | i/j        |                 |                 | Index2D   |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``mr``         | 2D  | char  | 0:255,0:210     | i/j        |                 |                 | Index2D   |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``eobstest``   | 3D  | short | 0:5,0:100,0:231 | t/Long/Lat | 25,-40 - 75,75  |  1950-01-01 ->  | Temporal +|
|                |     |       |                 |            |                 |  1950-01-06     | EPSG:4326 |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``mean_summer_ | 2D  | char  | 0:885,0:710     | Long/Lat   | 111.975,-44.525 |                 | EPSG:4326 |
| airtemp``      |     |       |                 |            | 156.275,-8.975  |                 |           |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``irr_cube_1`` | 3D  | short | 0:99,0:99,0:5   | i/j/k      |                 |                 | Index3D   |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+
| ``irr_cube_2`` | 3D  | float | 0:62,0:35,0:3   | E/N/ansi   | 75042.72735943, |  2008-01-01 ->  | EPSG:32633|
|                |     |       |                 |            | 5094865.557938- |  2008-01-08     | + ANSI    |
|                |     |       |                 |            | 705042.72735943,|                 |           |
|                |     |       |                 |            | 5454865.5579385 |                 |           |
+----------------+-----+-------+-----------------+------------+-----------------+-----------------+-----------+

These coverages are automatically inserted if missing.

Adding tests
------------

To add new tests to the test suite, simply add new WCS or WCPS queries
to the ``queries`` directory. Please adhere to the naming convention,
continuing from the last number:

+---------------+-----------------------------------------+
| **Type**      | **File name format**                    |
+---------------+-----------------------------------------+
| WCS KVP       | ``number-meaningful_name.[error.]kvp``  |
+---------------+-----------------------------------------+
| WCS XML       | ``number-meaningful_name.[error.]xml``  |
+---------------+-----------------------------------------+
| WCS SOAP      | ``number-meaningful_name.[error.]soap`` |
+---------------+-----------------------------------------+
| WCS REST      | ``number-meaningful_name.[error.]rest`` |
+---------------+-----------------------------------------+
| WCPS          | ``number-meaningful_name.[error.]test`` |
+---------------+-----------------------------------------+
| WCPS XML      | ``number-meaningful_name.[error.]xml``  |
+---------------+-----------------------------------------+
| rasql         | ``number-meaningful_name.[error.]rasql``|
+---------------+-----------------------------------------+

.. note::
    If the test is meant to raise an exception, add a further ``.error`` suffix to
    the file name before its extension, for both query and oracle.

The associated oracle (.oracle) files must also be added to the ``oracle/``
directory. The oracle can be automatically added by running the tests. In this
case it can be more convenient to run the tests on the single new query by
uncommenting this line in ``test.sh``: ::

   # uncomment for single test run
   [[ "$f" == 62-* ]] || continue

and choose the proper pattern to select one or more tests.



.. _code-guide:

*******************
rasdaman Code Guide
*******************

*Don't expect others to clean up your code*

An open-source project is fun, but it requires a great deal of discipline to
make all the code seamless that is coming from the developers worldwide. If
everybody just follow their individual coding style - no matter how ingenious
the code is - then the whole project will soon become unmaintainable.

To avoid this, rasdaman provides this code guide - don't worry, it contains as
few rules as possible, just enough to achieve overall coherence. Although
written for C++, *mutatis mutandis* it applies to Java, Javascript, and even
scripts.

* :ref:`Rules <code-guide-rules>` that have to be fulfilled strictly.
* :ref:`Recommendations <code-guide-recommendations>` which serve as suggestions for a 'better' coding style.
* :ref:`Examples <code-guide-examples>` to show how code should be written according to the guidelines.

Please understand that, while we always highly appreciate your contributions, we
may have to reject your patch if it breaks this code guide. Your successors
looking at the code will be most grateful for your efforts.

Credits: This code guide has been established by the rasdaman team based on the
codeguide originally developed by Roland Ritsch who in turn has crafted it along
the style guide of ELLEMTEL/Norway. Any eventual error is ours, of course.

.. _code-guide-rules:

Rules
=====

**Rule 0:** Every time a rule is broken, this must be clearly
documented.

---

**Rule 1:** Include files in C++ must have a file name extension *.hh*.

**Rule 2:** Implementation files in C++ must have a file name extension *.cc*.

**Rule 3:** Inline definition files must have a file name extension *.icc*.

**Rule 4:** Every file must include information about its purpose, contents, and
copyright. For this purpose, the several standard headers are provided
:ref:`here <code-guide-header-templates>`. Adjust the copyright to your name /
instituion as deemed adequate. All code must use a GPL header, except for files
in the raslib/, rasodmg/, and rasj/ directories, which must use an LGPL header.

**Rule 5:** All method definitions must start with a description of their
functionality using the `standard method header
<code-guide-standard-method-header>`_.

**Rule 6:** All comments must be written in English.

---

**Rule 7:** Every include file must contain a mechanism that prevents multiple
inclusions of the file.

**Rule 8:** Never use path name in ``#include`` directives. Only use relative
paths and the parent path (..) is not allowed.

**Rule 9:** Never have indirect inclusion of a function. Collective include
files are allowed.

---

**Rule 10:** The names of variables and functions must begin with a lowercase
letter. Multiple words must be written together, and each word that follows the
first starts with an uppercase letter (Camel Casing).

**Rule 11:** The names of constants must be all uppercase letters, words must be
separated by underscores ("\_").

**Rule 12:** The names of abstract data types, structures, typedefs, and
enumerated types must begin with an uppercase letter. Multiple words are written
together and each word that follows the first is begun with an uppercase letter
(Camel Casing).

---

**Rule 13:** The public, protected, and private sections of a class must be
declared in that order (the public section is declared before the protected
section which is declared before the private section). See the `standard class
definition <code-guide-standard-class-def>`_ for details.

**Rule 14:** No member functions within the class definition include file. The
only exception are inline functions.

**Rule 15:** No public or protected member data in a class. Use public inline
methods (``setVariable()`` and ``getVariable()``) to access private member data.

**Rule 16:** A member function that does not affect the state of an object (its
instance variables) must be declared const.

**Rule 17:** If the behavior of an object is dependent on data outside the
object, this data must not be modified by const member functions.

---

**Rule 18:** A class which uses ``new`` to allocate instances managed by the
class must define a copy constructor.

**Rule 19:** All classes which are used as base classes and which have virtual
function, must define a virtual destructor.

**Rule 20:** A class which uses ``new`` to allocate instances managed by the
class must define an assignment operator.

**Rule 21:** An assignment operator which performs a destructive action must be
protected from performing this action on the object upon which it is operating.

---

**Rule 22:** A public member function must never return a non-\`const\`
reference or pointer to member data.

**Rule 23:** A public member function must never return a non-``const``
reference or pointer to data outside an object, unless the object shares the
data with other objects.

---

**Rule 24:** Do not use unspecified function arguments (ellipsis notation).

**Rule 25:** The names of formal arguments to functions must be specified and
are to be the same both in the function declaration and in the function
definition.

---

**Rule 26:** Always specify the return type of a function explicitly. If no
value is returned then the return type is void.

---

**Rule 27:** A function must never return a reference or a pointer to a local
variable.

**Rule 28:** Do not use the preprocessor directive ``#define`` to obtain more
efficient code; instead, use inline functions.

---

**Rule 29:** Constants must be defined using const or enum; never use
``#define``.

**Rule 30:** Do not use numeric values directly in the code; use symbolic values
instead (Use constants for default values). Always document the meaning of the
value.

---

**Rule 31:** Variables must be declared with the smallest possible scope. Do not
use global variables.

**Rule 32:** Never declare multiple variables in the same line.

**Rule 33:** Every variable that is declared must be given a value before it is
used.

**Rule 34:** Don't use implicit type conversions.

**Rule 35:** Never cast an object to a virtual class.

**Rule 36:** Never convert a ``const`` to a non-``const``.

---

**Rule 37:** The code following a ``case`` label must always be terminated by a
``break`` statement.

**Rule 38:** A ``switch`` statement must always contain a ``default`` branch
which handles unexpected cases.

**Rule 44:** Never use ``goto``.

---

**Rule 45:** Do not use ``malloc``, ``realloc`` or ``free``, but use new and
``delete``. In general, use C++, not C code.

**Rule 47:** Always provide empty brackets (``[]``) for ``delete`` when
deallocating arrays.

**Rule 48:** Use C++ exception handling (try/catch) for every possible failure
situation.

---

**Rule 49:** When submitting a patch, describe concisely in the commit message
what has been accomplished in the patch. In case of a fix, include in the
message the ticket# fixed and place a comment in the source file
at the location the fix was done mentioning the ticket (best by its URL).


.. _code-guide-recommendations:

Recommendations
===============

**Recommendation 1:** Optimize code only if you know that you have a performance
problem. Think twice before you begin.

**Recommendation 2:** Eliminate all warnings generated by the compiler.

**Recommendation 3:** An include file should not contain more than one class
declaration.

**Recommendation 4:** Place machine-dependent code in a special file so that it
may be easily located when porting code from one machine to another.

**Recommendation 5:** Always give a file a name that is unique in as large a
context as possible.

**Recommendation 6:** An include file for a class should have a file name of the
form + .hh. Use all lowercase letters.

**Recommendation 7:** Use the directive #include "filename.hh" for user-prepared
include files.

**Recommendation 8:** Use the directive #include for include files from system
libraries.

**Recommendation 9:** Choose names that suggest the usage. Don't give generic
names to variables.

**Recommendation 10:** Encapsulate global variables and constants, enumerated
types, and typedefs in a class.

**Recommendation 11:** Always provide the return type of a function explicitly
on a separate line, together with template or inline specifiers.

**Recommendation 12:** When declaring functions, the leading parenthesis and the
first argument (if any) are to be written on the same line as the function name.
If space permits, other arguments and the closing parenthesis may also be
written on the same line as the function name. Otherwise, each additional
argument is to be written on a separate line (with the closing parenthesis
directly after the last argument).

**Recommendation 13:** Always write the left parenthesis directly after a
function name (no blanks). Use 'astyle --style=allman -c -n' for autoformatting
your code.

**Recommendation 14:** Braces (``{ }``) which enclose a block are to be placed in
the same column as the outer block, on separate lines directly before and after
the block. Use indentation of four spaces and don't use tab stops. Use ``astyle
--style=allman -c -n`` for autoformatting your code.

**Recommendation 15:** The reference operator \* and the address-of operator &
should be directly connected with the type names in declarations and
definitions. Use ``astyle --style=allman -c -n`` for autoformatting your code.

**Recommendation 16:** Do not use spaces around ``.`` or ``->``, nor between unary
operators and operands. Use ``astyle --style=allman -c -n`` for autoformatting
your code. Got it? ;-)

**Recommendation 17:** An assignment operator should return a const reference.

**Recommendation 18:** Use references instead of pointers whenever possible.

**Recommendation 19:** Use constant references (const &) instead of
call-by-value, unless using a pre-defined data type or a pointer.

**Recommendation 20:** Avoid long and complex functions.

**Recommendation 21:** Avoid pointers to functions.

**Recommendation 22:** Pointers to pointers should be avoided whenever possible.

**Recommendation 23:** Use a typedef to simplify program syntax when declaring
function pointers.

**Recommendation 24:** Always use unsigned for variables which cannot reasonably
have negative values.

**Recommendation 25:** Always use inclusive lower limits and exclusive upper
limits.

**Recommendation 26:** Avoid the use of continue.

**Recommendation 27:** Do not write logical expressions of the type ``if (test)`` or
``if (!test)`` when test is a pointer.

**Recommendation 28:** Use parentheses to clarify the order of evaluation for
operators in expressions.

**Recommendation 29:** Do not allocate memory and expect that someone else will
deallocate it later.

**Recommendation 30:** Always assign NULL to a pointer after deallocating
memory.

**Recommendation 31:** Check the return codes from library functions even if
these functions seem foolproof.

**Recommendation 32:** If possible, always use initialization instead of
assignment. To declare a variable that has been initialized in another file, the
keyword extern is always used.

**Recommendation 33:** Avoid implicit type conversions (casts).

**Recommendation 34:** Use all flavors of const as often as possible.



.. _code-guide-examples:

Examples
========

.. _code-guide-header-templates:

Standard Include Header
-----------------------

.. code-block:: cpp

    /*
    * This file is part of rasdaman community.
    *
    * Rasdaman community is free software: you can redistribute it and/or modify
    * it under the terms of the GNU General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * Rasdaman community is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU General Public License for more details.
    *
    * You should have received a copy of the GNU General Public License
    * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    *
    * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    *
    * For more information please see <http://www.rasdaman.org>
    * or contact Peter Baumann via <baumann@rasdaman.com>.
    */
    /*************************************************************
     *
     * PURPOSE:
     *
     * COMMENTS:
     *
     * BUGS:
     *
     ************************************************************/

Standard Include Header (LGPL)
------------------------------

.. code-block:: cpp

    /*
    * This file is part of rasdaman community.
    *
    * Rasdaman community is free software: you can redistribute it and/or modify
    * it under the terms of the GNU Lesser General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * Rasdaman community is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU Lesser General Public License for more details.
    *
    * You should have received a copy of the GNU Lesser General Public License
    * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    *
    * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    *
    * For more information please see <http://www.rasdaman.org>
    * or contact Peter Baumann via <baumann@rasdaman.com>.
    */
    /*************************************************************
     *
     * PURPOSE:
     *
     * COMMENTS:
     *
     * BUGS:
     *
     ************************************************************/

Standard Source Headers
-----------------------

.. code-block:: cpp

    /*
    * This file is part of rasdaman community.
    *
    * Rasdaman community is free software: you can redistribute it and/or modify
    * it under the terms of the GNU General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * Rasdaman community is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU General Public License for more details.
    *
    * You should have received a copy of the GNU General Public License
    * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    *
    * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    *
    * For more information please see <http://www.rasdaman.org>
    * or contact Peter Baumann via <baumann@rasdaman.com>.
    */
    /*************************************************************
     *
     * PURPOSE:
     *
     * COMMENTS:
     *
     * BUGS:
     *
     ************************************************************/

Standard Source Header (LGPL)
-----------------------------

.. code-block:: cpp

    /*
    * This file is part of rasdaman community.
    *
    * Rasdaman community is free software: you can redistribute it and/or modify
    * it under the terms of the GNU Lesser General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * Rasdaman community is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU Lesser General Public License for more details.
    *
    * You should have received a copy of the GNU Lesser General Public License
    * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    *
    * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    *
    * For more information please see <http://www.rasdaman.org>
    * or contact Peter Baumann via <baumann@rasdaman.com>.
    */
    /*************************************************************
     *
     * PURPOSE:
     *
     * COMMENTS:
     *
     * BUGS:
     *
     ************************************************************/


Standard Inline Header
----------------------

.. code-block:: cpp

    /*
    * This file is part of rasdaman community.
    *
    * Rasdaman community is free software: you can redistribute it and/or modify
    * it under the terms of the GNU General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * Rasdaman community is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU General Public License for more details.
    *
    * You should have received a copy of the GNU General Public License
    * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    *
    * Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    *
    * For more information please see <http://www.rasdaman.org>
    * or contact Peter Baumann via <baumann@rasdaman.com>.
    /
    /**
     * INLINE SOURCE:
     *
     * MODULE:
     * CLASS:
     *
     * COMMENTS:
     *
    */


Standard Script / Make Header
-----------------------------

.. code-block:: make

    #
    # MAKEFILE FOR:
    #
    # This file is part of rasdaman community.
    #
    # Rasdaman community is free software: you can redistribute it and/or modify
    # it under the terms of the GNU General Public License as published by
    # the Free Software Foundation, either version 3 of the License, or
    # (at your option) any later version.
    #
    # Rasdaman community is distributed in the hope that it will be useful,
    # but WITHOUT ANY WARRANTY; without even the implied warranty of
    # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    # GNU General Public License for more details.
    #
    # You should have received a copy of the GNU General Public License
    # along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    #
    # Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    #
    # For more information please see <http://www.rasdaman.org>
    # or contact Peter Baumann via <baumann@rasdaman.com>.
    # Top Level makefile. This points to the various modules that have to be build
    # and/or deployed
    #
    #
    # COMMENTS:
    #
    ##################################################################

Standard Script / Make Header (LGPL)
------------------------------------

.. code-block:: cpp

    #
    # MAKEFILE FOR:
    #
    # This file is part of rasdaman community.
    #
    # Rasdaman community is free software: you can redistribute it and/or modify
    # it under the terms of the GNU Lesser General Public License as published by
    # the Free Software Foundation, either version 3 of the License, or
    # (at your option) any later version.
    #
    # Rasdaman community is distributed in the hope that it will be useful,
    # but WITHOUT ANY WARRANTY; without even the implied warranty of
    # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    # GNU Lesser General Public License for more details.
    #
    # You should have received a copy of the GNU Lesser General Public License
    # along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
    #
    # Copyright 2003 - 2018 Peter Baumann / rasdaman GmbH.
    #
    # For more information please see <http://www.rasdaman.org>
    # or contact Peter Baumann via <baumann@rasdaman.com>.
    #
    #
    # COMMENTS:
    #
    ##################################################################

Recomendation 12
----------------

Correct:

.. code-block:: cpp

    inline int
    getLenght()
    {
        ...
    }


Wrong:

.. code-block:: cpp

    inline int getLenght()
    {
        ...
    }


Macros vs inline functions
--------------------------

Wrong:

.. code-block:: cpp

    #define SQUARE(x) ((x)*(x))         // wrong
    int a = 2
    int b = SQUARE(a++)                 // a == 6

Right:

.. code-block:: cpp

    inline int
    square( int x );                    // right
    {
      return (x*x)
    }
    int c = 2;
    int d = square(c++);                // d == 4


Constants vs Standalone Values
-------------------------------

Wrong:

.. code-block:: cpp

    if (iterations <= 0)
        iterations = 5;

Correct:

.. code-block:: cpp

    // Default number of iterations in units
    const int defaultIterationsNumber = 5;

    ...

    if (iterations <= 0)
        iterations = defaultIterationsNumber;


Macros vs const variables
-------------------------

.. code-block:: cpp

    #define BUFSIZE 7            // no type checking

    const int bufSize = 7        // type checking takes place

    enum  size { BufSize = 7 };  // type checking takes place


.. _code-guide-standard-method-header:

Standard Method Declaration
---------------------------

.. code-block:: cpp

    /**
    * Description of addNumbers
    * @param n1 the first argument.
    * @param n2 the second argument.
    * @return The return value
    */
    template <class P>
    int
    addNumbers(int n1, int n2)
    {
        ...
    }


Case statement
--------------

.. code-block:: cpp

    switch(tag)
    {
      case A:
        // do something
        // break is missing and foo() is also called in case A    // wrong

      case B:
        foo();
        // do something else
        break;

      default:
        // if no match in above cases, this is executed
        break;
    }

Dynamic array allocation and deallocation
-----------------------------------------

.. code-block:: cpp

    int n = 7
    T* myT = new T[n];  // T is type with defined constructors and destructors

    //........

    delete myT;         // No! Destructor only called for first object in array a.
    delete [10] myT ;   // No! Destructor called on memory out of bounds in array a.
    delete [] myT ;     // OK, and always safe.


.. _code-guide-standard-class-def:

Standard Class Definition
--------------------------

Example class definitions in accordance with the style rules

.. code-block:: cpp

    class String : private Object
    {
    public:
        String();
        String(const String&);
        unsigned getLenght() const;
        inline Encoding getEncoding() const;
        inline void setEncoding(Encoding newEncoding);

    protected:
        int checkIndex( unsigned index ) const;

    private:
        unsigned noOfChars;
        Encoding encoding;

    };

Wrong:

.. code-block:: cpp

    class String
    {
      public:
        int getLength() const // No !!
        {
          return length;
        };

      private:
        int length;
    };

Correct:

.. code-block:: cpp

    class String
    {
      public:
        int getLength() const;

      private:
        int length;
    };

    inline int
    String::getLength() const
    {
      return len ;
    }

Classes with dynamic member data
--------------------------------

Declaration examples of the assignment operator:

.. code-block:: cpp

    MySpezialClass&
    MySpezialClass::operator= (const MySpezialClass msp);     // no

    void
    MySpezialClass::operator= (const MySpezialClass msp);     // well

    const MySpezialClass&
    MySpezialClass::operator= (const MySpezialClass msp);     // recommanded

    Class definition

    class DangerousBlob
    {
      public:
        const DangerousBlob& operator=(const DangerousBlob& dbr);

      private:
        char* cp;
    };

Definition of assignment operator:

.. code-block:: cpp

    const DangerousBlob&
    DangerousBlob::operator=(const Dangerous& dbr)
    {
      if ( this != &dbr )          // Guard against assigning to the "this" pointer
      {
        // ...
        delete cp;                 // Disastrous if this == &dbr
        // ...
      }
    }

Constant references as return types:

.. code-block:: cpp

    class Account
    {
      public:
        Account ( int myMoney ): moneyAmount(myMoney) { };
        const int& getSafemoney()  const { return moneyAmount;};
        int&       getRiskyMoney() const { return moneyAmount;};  // no

      private:
         int moneyAmount;
    };

    Account myAcc(10);
    myAcc.getSafeMoney()  += 100000;  // compilation error: assignment to constant
    myAcc.getRiskyMoney() += 1000000; // myAcc::moneyAmount = 1000010 !!

.. note::
    Method definition within the class definition is forbidden by rule.

Parameter declaration
---------------------

.. code-block:: cpp

    int setPoint( int, int )     // wrong
    int setPoint( int x, int y )

    int
    setPoint( int x, int y )
    {
      //....
    }

Return type
-----------

.. code-block:: cpp

    int
    calculate ( int j )
    {
      return 2*j;
    }

    void
    noReturnType( char* xData, char* yFile)
    {
      //....
    }

Include directive
-----------------

.. code-block:: cpp

    // file is PrintData.cc

    #include "PrintData.hh"    // user include file

    #include <iostream.h>      // include file of the system library

Avoid global data
-----------------

.. code-block:: cpp

    class globale
    {
      public:
        //........

      protected:
        const char* functionTitle = "good style";

        int   constGlobal;
        char* varGlobal;
    }

Formating of functions
----------------------

.. code-block:: cpp

    void foo (); // no
    void foo();  // better

    // right
    int
    myComplicateFunction( unsigned unsignedValue,
                          int intValue
                          char* charPointerValue );

    // wrong
    int myComplicateFunction (unsigned unsignedValue, int intValue char* charPointerValue);

Formating of pointer and reference types
----------------------------------------

.. code-block:: cpp

    char*
    object::asString()
    {
      // something
    };

    char* userName = 0;
    int   sfBlock  = 42;
    int&  anIntRef = sfBlock;

Assignment operator
-------------------

.. code-block:: cpp

    MySpezialClass&
    MySpezialClass::operator=( const MySpezialClass& msp ); // no

    const MySpezialClass&
    MySpezialClass::operator=( const MySpezialClass& msp ); // recommended

Reference vs pointer
--------------------

.. code-block:: cpp

    // Unnecessarily complicated use of pointers
    void addOneComplicated ( int* integerPointer )
    {
      *integerPointer += 1:
    }
    addOneComplicated (&j)


    // Write this way instead
    void addOneEasy ( int& integerReference )
    {
      integerReference +=1:
    }
    addOneEasy(i);

Call-by-value vs call-by-constant-reference
-------------------------------------------

.. code-block:: cpp

    // this may lead to very inefficient code.
    void foo( string s );
    string a;
    foo(a)               // call-by-value

    // the actual argumment is used by the function
    // but it connot be modified by the function.
    void foo( const string& s );
    string c;
    foo(c);              // call-by-constant-reference

Avoid continue
--------------

.. code-block:: cpp

    while ( /* something */ )
    {
      if (/* something */)
      {
        // do something
        continue;                // Wrong!
      }
      // do something
    }

    // By using an extern 'else' clause, continue is avoided and the code
    // is easier to understand

    while ( /* something */ )
    {
      if (/* something */)
      {
        // do something
      }
      else
      {
        // do something
      }
    }

Parentheses
-----------

.. code-block:: cpp

    // Interpreted as (a<b)<c, not (a<b) && (b<c)
    if (a<b<c)
    {
      //...
    }

    // Interpreted as a & (b<8), (a&b) <8
    if (a & b<8)
    {
      //..
    }

    // when parentheses are recommended
    int i = a>=b && c < d && e+f <= g+h;        // no
    int j = (a>=b)&&(c<d) && (( e+f) <= (g+h)); // better


Include Files
-------------

Include file for the class ``PackableString``:

.. code-block:: cpp

    #ifndef PACKABLESTRING_HH
    #define PACKABLESTRING_HH

    #include "string.hh".
    #include "packable.hh".

    /**
     * A test class with elaborate description.
    /*

    class Buffer:public String:public Packable
    {
      public:
        class PackableString (const String& s);
        class Buffer* put (class Buffer* outbuffer);
        //.......
    };

    #endif

Implementation file for the class ``PackableString``:

.. code-block:: cpp

    // PackableString.cc
    // not recommanded <../include/iostream.h> Wrong

    #include <iostream.h> // Right
    #include "PackableString.hh"
    // to be able to use Buffer instances, buffer.hh must be included.
    #include "buffer.hh"

    Buffer*
    PackableString::put(Buffer* outbuffer)
    {
        //......
    }

**************
Geo services
**************

Petascope Developer's Documentation
===================================

Introduction
------------

This page serves as an introduction to the petascope component from 
a developer's perspective (see also :ref:`sec_geo-services-guide`).

Petascope is built on the **Spring Boot Framework** with **Hibernate** as object relational
mapping data model for backend-communication with petascopedb;
Implements support for the Coverage Schema Implementation (CIS version 1.0: 
*GridCoverage*, *RectifiedGridCoverage* and *ReferenceableGridCoverage* 
and CIS version 1.1: *GeneralGridCoverage* which is the unified class
for coverage types in CIS 1.0).

Petascope can be deployed on more backend DBMS beside PostgreSQL
like HSQLDB, H2, etc. Postgresql is still the most stable database
for deploying petascope, but the user can switch to other databases
by changing the configuration in petascope.properties.

The Spring Boot Framework provides many utilities that aid 
in quicker development of petascope. Petascope can now start as an embedded
web application with an internal embedded Tomcat (i.e: 
no need to deploy to external Tomcat).

Code
----

Petascope is divided in 3 applications:

* `​core <http://rasdaman.org/browser/applications/petascope/petascope_core>`_ contains the classes
  to generate petascopedb's tables by **Hibernate** with **Liquibase** and other
  utilities classes. This is the core library used by other petascope's applications.

* `​main <http://rasdaman.org/browser/applications/petascope/petascope_main>`_ contains the classes
  to handle WCS, WCPS, WMS, WCST-T requests and generates rasql queries
  for rasdaman. This is the **rasdaman.war** application to be deployed
  to external **Tomcat** or started in embedded mode with ``java -jar rasdaman.war``.

* `migration <http://rasdaman.org/browser/applications/petascope/petascope_migration>`_ handles petascopedb migration
  (**must need when updating from v9.4 to v9.5+**) using Liquibase;
  it can also migrates petascopedb from Postgresql to another DBMS like H2 or HSQLDB.

Database migration
^^^^^^^^^^^^^^^^^^

To support different kinds of databases, we use ​**Liquibase**, which creates
the changes for each update in XML and uses that to generate the SQL statements
for the target database (e.g: Postgresql, HSQLDB, H2, etc). To further
understand how **Liquibase** works to populate database tables, see comments in
the `​liquibase.properties <http://rasdaman.org/browser/applications/petascope/petascope_main/src/main/resources/liquibase.properties>`_ config file.


CRS management
^^^^^^^^^^^^^^

Petascope relies on a **SECORE Coordinate Reference System (CRS)** resolver
that can provide proper metadata on a coverage's native CRS. One can either
deploy a local SECORE instance, or use the official ​OGC SECORE resolver
at ​http://www.opengis.net/def/.

It currently keeps a few internal caches, especially for SECORE CRS
resources and responses: the gain is both on performance and on robustness
against network latencies. Caching information about CRSs is safe as CRSs
can be considered static resources - normally they do not change
(and with the CRS versioning recently introduced by OGC a particular CRS
version never will change indeed).

It is suggested to run a *WCS GetCapabilities* after a fresh new deployment,
so that the CRS definitions of all the offered coverages are cached:
after that single request, mainly almost all the CRS-related information
has already been cached.

The **CrsUtil** class serves several purposes:

* CRS definitions: the relevant information parsed from a GML CRS definition
  is stored as a CrsDefinition object. This includes both spatial and temporal
  reference systems;

* CRS equivalence tests: thanks to the ``/equal`` endpoint of SECORE,
  effective equivalence (no simple string comparison) between
  two reference systems can be verified. This operation is required
  when checking if a CRS has been cached or not: as an example, 
  KVP notation of a CRS URI is independent of the order of key/value pairs, 
  so that ​http://www.opengis.net/def/crs?authority=EPSG&version=0&code=32633 
  and ​http://www.opengis.net/def/crs?version=0&authority=EPSG&code=32633 
  are equivalent despite their different URI identifier.

Testing
^^^^^^^

The `​systemtest/testcase_services <http://rasdaman.org/browser/systemtest/testcases_services>`_
covers all the possible cases for WCS, WCPS, WMS and WCS-T. The easiest way
to understand how Petascope works is by running some tests
and debug it with your IDE (e.g: NetBeans, IntelliJ IDEA,...).

For instance: send this request in Web Browser with deployed petascope in Tomcat:
​http://localhost:8080/rasdaman/ows?service=WCS&version=2.0.1&request=GetCapabilities. 
Then you can set a debug in class ``petascope.controller.PetascopeController``
of **petascope-main** application, then follow all following classes when debugging
to understand how the request is handled inside petascope.

Warnings
^^^^^^^^

Don't create ``BigDecimal`` directly from a ``double`` variable,
rather from ``double.toString()``.
E.g. ``BigDecimal a = new BigDecimal(0.2356d)`` will result with random
fraction numbers after the real value
of double (*0.235653485834584395929090423904902349023904290349023904*);
subsequently this would lead to wrong coefficient calculation in petascope.

WSClient Developer's Documentation
====================================

Introduction
------------

WSClient is a frontend Web application which facilitates interactions
from users to petascope (OGC WCS/WCS-T/WCPS/WMS standards implementation).
It it built based on AngularJS framework version 1.4 with other libraries
like CSS Bootstrap and WebWorldWind to make a single page application.

When building petascope, WSClient is added to *rasdaman.war*.
This is then deployed to Tomcat.
Example of deployed WSClient folder in external Tomcat:

::

  /var/lib/tomcat/webapps/rasdaman/WEB-INF/classes/public/WSClient/ 


Code
----

WSClient uses TypeScript language rather Javascript directly. To compile
WSClient, developers need to install some dependencies:

* *npm* - Node package manger. Example:

::
   
   sudo yum install npm

* *bower* - Used for managing dependencies. Example:

::

   sudo npm install -g bower   

* *Typescript* - Used for compiling .ts files to .js. Example:

::

   sudo npm install -g tsc

* *TSD* - Used for retrieving typings.  Example:

::

   sudo npm install -g tsd


Once all dependencies are installed, in the source folder of WSClient
(``application/wcs-client``) run these commands *once*:

::
  
   npm install
   tsd install
   bower install

Then, everytime a new feature/fix is added, one needs to compile
from TypeScript to Javascript files to work in Web Browsers  with the following
command in WSClient source folder:

::
  
   tsc
   
After that, 2 important files in ``application/WSClient/app`` folder
``main.js`` and ``main.js.map`` are generated which need to be included
in the patch besides other added/updated files.


SECORE Developer's Documentation
================================

Introduction
------------

SECORE (Semantic Cordinate Reference System Resolver) is a server which
resolves CRS URLs into full CRS definitions represented in GML 3.2.1. 
Offical SECORE of rasdaman is hosted at: http://www.opengis.net/def.

Same as Petascope, SECORE builds on Spring framework. However, as it is an
XML database resolver (mainly all CRSs are occupied from
`EPSG releases <https://www.epsg-registry.org/>`_), hence it does not rely
on any relational database as petascopedb. 

Code
----

SECORE stores and queries XML data in a ​BaseX XML database. On the disk
this database is stored in ``$CATALINA_HOME/webapps/secoredb``
(e.g: ``/var/lib/tomcat/webapps``), this is the directory where
external Tomcat process will typically have write access.
The database is created and maintained automatically, so no action by the user
is required regarding this.

In SECORE, there are 2 types of GML Database (*UserDictionary.xml*
and *GmlDictionary.xml*). User will *add/update/delete* CRSs **only**
in *UserDictionary.xml* when *GmlDictionary.xml* comming from EPSG releases
are intact. 

SECORE database tree can be viewed and (upon login) modified via
graphical web interface at "http://your.server/def/index.jsp".

More generally, any folder and definition can turn to EDIT mode by appending
a **/browse.jsp** to its URI:
e.g. "http://your.server/def/uom/EPSG/0/9001/browse.jsp" will
let you *view/edit* EPSG:9001 unit of measure, whereas
"http://your.server/def/uom/EPSG/0/browse.jsp" will let you either
*remove* EPSG UoM definitions or *add a new one*, not necessarily
under the EPSG branch: the **gml:identifier** of the new definition
will determine its position in the tree.

As explained in the ​`related publication <http://link.springer.com/chapter/10.1007%2F978-3-642-29247-7_5>`_,
SECORE supports *parametrization of CRSs* as well: with this regard, you should mind
that relative ​XPaths are not allowed (either start
with */* or *//* when selecting nodes); non-numeric parameters
must be embraced by single or double quotes both when setting optional
default values in the definition or when setting custom values in the URI.

Update new EPSG version
-----------------------

When EPSG announces a new release, one can download the new GML dictionary file
from this link: http://www.epsg-registry.org.

From the downloaded .zip file, extract *GmlDictionary.xml* file inside and add it
to `SECORE secore database <http://rasdaman.org/browser/applications/secore/src/main/resources/gml.tar.gz>`_
under a folder with version name (e.g: ``9.4.2/GmlDictionary.xml``).

After that, build SECORE normally to have a new web application *def.war*
and redeploy it to Tomcat server. Finally, check if a new EPSG version is added
from http://your.server/def/EPSG/. Example:

::
  
  <identifiers xmlns="http://www.opengis.net/crs-nts/1.0" 
     xmlns:gco="http://www.isotc211.org/2005/gco"
     xmlns:gmd="http://www.isotc211.org/2005/gmd"
     at="http://localhost:8080/def/crs/EPSG/">
     <identifier>http://localhost:8080/def/crs/EPSG/0</identifier>
     <identifier>http://localhost:8080/def/crs/EPSG/8.5</identifier>
     <identifier>http://localhost:8080/def/crs/EPSG/8.9.2</identifier>
     <identifier>http://localhost:8080/def/crs/EPSG/9.4.2</identifier>
  </identifiers>






