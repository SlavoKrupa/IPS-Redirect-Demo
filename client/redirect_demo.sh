#!/bin/sh

mvn -q exec:java -Dexec.mainClass=com.redhat.xpaasqe.RedirectDemo -Dexec.args=""
