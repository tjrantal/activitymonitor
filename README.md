Source code of the [Activity Reminder & PA tracker](https://play.google.com/store/apps/details?id=timo.home.activityMonitor&hl=en) Android app written by Timo Rantalainen tjrantal at gmail dot com.  Released into the public domain with [Creative Commons Attribution CC-BY license](https://creativecommons.org/licenses/by/4.0). Any licenses applied by the code I have copied from others still apply.

COMPILING
I use command line tools from Linux command line to build the code, and hence no Android studio 'project' files are included. Hopefully you'll be able to create the missing files automagically in case you prefer an IDE... Look into the gradle.build for dependencies.

OF NOTE
I decided to use inner classes rather than creating additional files for the classes (felt like a good idea at the time...). That is, you'll have to scroll down in the files to find the 'hidden/missing' classes.