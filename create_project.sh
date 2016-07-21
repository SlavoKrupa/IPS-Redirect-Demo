#!/bin/sh

oc new-project ips-redirect-demo
oc create -f processserver-app-secret.json
oc create -f jboss-processserver63-openshift.json
oc create -f processserver63-amq-postgresql-persistent-s2i.json
sleep 3s
oc import-image jboss-processserver63-openshift

