#!/bin/bash
java -cp find-optool.jar org.apache.niolex.address.optool.ShellMain -server localhost:9181 -root find -timeout 5000 $*

