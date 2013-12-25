#!/bin/bash
java -cp find-optool.jar org.apache.niolex.address.optool.ShellMain -server 10.34.130.92:9181 -root find -timeout 5000 $*

