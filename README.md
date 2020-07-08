# VANTED #

For installation, please visit the main web page at
[vanted.org](http://vanted.org).
Here, you find the most-recent version of the source code.

## Overview ##

VANTED is a Java based extendable network visualisation and analysis tool with focus on applications in the life sciences.

It allows users to create and edit networks, as well as mapping experimental data onto networks. Experimental datasets can be visualized on network elements as graphical charts to show time series data or data of different treatments, as well as environmental conditions in the context of the underlying biological processes. Built-in statistical algorithms allow an easy and fast evaluation of mapped data (e.g. t-Test or correlation analysis).

The functionality of VANTED can be extended by installing one or more of the provided Addons using the built-in Addon Manager. For an overview see: [here](http://vanted.org/add-ons).

New Addons can be developed from scratch, using built-in [API](https://kim25.wwwdns.kim.uni-konstanz.de/vanted/javadoc/)s for manipulation of networks and data. A simple step-by-step guide for developing custom Addons can be found [here](https://github.com/LSI-UniKonstanz/vanted/wiki).

The development of VANTED is an ongoing process and we try to fix bugs and implement new features as soon as possible. Updates will be delivered on a regular basis.

## Contact ##

If you have questions or suggestions regarding VANTED, please contact us at:
[feedback@vanted.org](mailto:feedback@vanted.org)

## News ##

### VANTED v2.7.0 release (08/07/2020) ###
    Minimum Java 8 compatibility
    Bugfixes: Clustering
    RIMAS server migration
    HTTPS server connections
    Resolves HTTP-HTTPS redirecting
    BioModels: Handles parsing exception in queries
    Add-on Manager and Add-on Pane improvements
    Undo/Redo additions
    Usability improvements:
	Menu updates
	First graph window fits in pane
	Undo limit increases to 20 operations
    Standard format becomes GML
    Internal API improvements
    Replaces non-API Sun references

### VANTED v2.6.5 release (29/03/2018) ###

    Java 9 Compatibility Update
    Several algorithms improved, including
        Find and Layout Circles
        Merge Nodes
        Erdos-Renyi Graph Generation
        Watts-Strogatz Graph Generation
        Grubbs' Test
        David Test
    Minor BioModels update
    OS X in-graph grid movement fix
    Overall internal improvements

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.5).

### VANTED v2.6.4 release (09/12/2017) ###

    High DPI Support feature
    Nimbus LookAndFeel is now disabled
    Edge selection positioning
    Metacrop stability improvements
    Detached mode update
    Node Functions enhancements
    Server update
    Windows Installer update
    Overall performance and stability improvements
    Minor bug fixes

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.4).

### VANTED v2.6.3 release (16/09/2016) ###

    Fixed drawing problems of nodes with border size 1
    Fixed GraphML reader.
    Added support for more SBGN shapes
    Added BioModels backup webservice location
    Changed MetaCrop maps to support new SBGN Shapes

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.3).

### VANTED v2.6.2 release (09/03/2016) ###

    Minor bugfixes
    Native support for more SBGN Shapes
    Relative positioning of labels

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.2).

### VANTED v2.6.1 release (19/01/2016) ###

    Bugfixes including broken compatibility with SBGN-Ed
    Update to new jsbml 1.1 library
    Changes and bugfixes in APIs for Developers

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.1).

### VANTED v2.6.0 release (27/11/2015) ###
Bugfixes:

    Code clean-up and removal of bugs using bug-scanner tools
    Cluster background was not exported to image
    Introducing edge bends is now working properly
    Sometimes specific changes didn't visually appear and a redraw had to be triggered

Improvements:

    Update scan improved to support md5-hash and includes add-ons as well
    Performance improvement for larger networks (30k Nodes and more)
    Support for the SBML-Layout extension
    Added Animation Framework
    Selection frames can now be drawn inside nodes using Ctrl+Shift
    Nodes can be created inside other nodes using Ctrl+Shift

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.6.0).

### VANTED v2.5.3 release (05/10/2015) ###
Bugfixes:

    start up problems, when no previous preferences were found to select LookAndFeel
    several minor bugfixes found and fixed throughout heavy usage

Improvements:

    the "Remove Connecting Nodes" algorithm will now not only remove nodes and keep the overall connection of the network, but also respect the original direction
    the ESC key will also deselect network elements

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.5.3).

### VANTED v2.5.2 release (16/09/2015) ###
This release contains only minor bugfixes which improve user experience and speed.

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.5.2).

### VANTED v2.5.1 release (06/09/2015) ###
Important !!!

Due to a bug in the loading process the SBML loader was broken.

This has been fixed but you need to install the new release of VANTED using the installer. This bug will not be fixed using the automated VANTED update.

You can download and install this version [here](http://kim25.wwwdns.kim.uni-konstanz.de/vanted/release/2.5.1).

Please excuse us for the inconvenience.

Users who went through the automatic update to get 2.5.1 will see an error message when trying to load SBML documents. This error message will not appear for users who did a fresh install of VANTED 2.5.1.

### VANTED v2.5.0 release (01/09/2015) ###
Changes are listed below:

    Drawing speed improvement when loading and exploring large networks
    Reorganisation of menus and sidepanels
    Support for downloads of curated SBML models from Biomodels database
    Reenabling support to create GO and KEGG hierarchies
    Better support for program preferences
    Tons of bug fixes

### VANTED v2.2.0 release (23/12/2014) ###
Changes are listed below:

    Updated support for new KEGG REST API
    bug fixes
