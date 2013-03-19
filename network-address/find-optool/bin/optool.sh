#!/bin/bash
java -cp find-optool.jar org.apache.niolex.address.optool.ShellMain -server 10.22.241.233:8181 -root find -timeout 5000 $*

