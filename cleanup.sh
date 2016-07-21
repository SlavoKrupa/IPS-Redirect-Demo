#!/bin/sh

oc delete project ips-redirect-demo
curl -X DELETE http://maven.latest.xpaas/api/hosted/local-deployments/com/redhat/xpaasqe/UpdateDemo/1.0.0/
curl -X DELETE http://maven.latest.xpaas/api/hosted/local-deployments/com/redhat/xpaasqe/UpdateDemo/1.0.1/
curl -X DELETE http://maven.latest.xpaas/api/hosted/local-deployments/com/redhat/xpaasqe/UpdateDemo/
curl -X DELETE http://maven.latest.xpaas/api/hosted/local-deployments/com/redhat/xpaasqe/UpdateDemo

