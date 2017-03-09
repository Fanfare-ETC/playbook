Fanfare
-------

[![Build Status](https://travis-ci.org/Fanfare-ETC/playbook.svg?branch=master)](https://travis-ci.org/Fanfare-ETC/playbook)

Fanfare is a project by a group of six students from Carnegie Mellon University
Entertainment Technology Center (CMU-ETC). We have teamed up with Verizon to
further invigorate the stadium atmosphere. To do this, we have created a
stadium-exclusive experience that encourages interaction with other crowd
members. Our goal is to engage fans with the stadium and each other in a new
and innovative way, so they won't just come back - they'll bring their friends
next time, too.

For more information about the project, visit http://www.etc.cmu.edu/projects/fanfare/.

Playbook
--------

Playbook is an in-stadium fan experience that allows baseball fans to predict
and collect plays. It currently targets Android devices.

Setup
-----

Requirements:

- [Android Studio and SDK](https://developer.android.com/studio/index.html)
- [Android NDK](https://developer.android.com/ndk/index.html)
- [cocos2d-x](http://www.cocos2d-x.org/) (included in repository)

The exact requirements may vary across different platforms.

Building
--------

Once the dependencies are installed, you can build the project just like any
other Android project using Android Studio or `gradlew`.

To build from the command line, use:

    ./gradlew assembleDebug

Once done, the built files can be found in `app/build/outputs/apk/`.

Release
-------

To release, an Android signing key is required. For more information on how to
create a keystore and sign the APK in release mode, check this
[link](https://developer.android.com/studio/publish/app-signing.html#release-mode).

Copyright
---------

Copyright (c) 2017 The Fanfare Team

This project is licensed under the MIT License. Permission is hereby granted,
free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and
to permit persons to whom the Software is furnished to do so, subject to the
following conditions: 

The above copyright notice and this permission notice shall be included in 
all copies or substantial portions of the Software. 

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
THE SOFTWARE. 

