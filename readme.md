A simple utility to allow you to transfer files between computers on a local network.

The project is built to work with any version of Java above 1.5, as 1.5 is the last version of Java to be supported on PowerPC OS X. Given I built this project mainly to get around AFP and SMB not really working with 10.4 Tiger anymore, that seems like a reasonable point to cut off Java support.

By no means is this a particularly good or secure idea, given the versions of Java included with OS X 10.4 and 10.5 are horrifically insecure and haven't been updated for over a decade, so be warned if you use this.
I opted to use Java because it's installed by default on all machines running both of the aforementioned OSe, and also gives me a good excuse to learn Java.

[This article](https://tenfourfox.blogspot.com/2012/05/security-blanket-blues.html) outlines a lot of the security issues faced by PowerPC versions of Mac OS X and may be worth a read if you are interested. It's over 10 years old now (May 9th, 2012), so things have only got worse since!

The plan for this is mainly just working on whatever I feel like when I feel like it, so the Versions document is very much a working document and is subject to change.

Currently implemented and (mostly) complete features include:
- Hosting files:
  - Host a file for other machines to connect and retrieve
  - Change the file being hosted without restarting (requires clients to reconnect)
  - Select files from either the command line or GUI
  - Prevent anybody from connecting by setting a password
- Retrieving the files:
  - Connect to another machine to retrieve the file
  - Disconnect from one machine and reconnect to another, without restarting the program
