#!/bin/bash

set -e

echo $GCLOUD_PASS | base64 --decode -i > ${HOME}/gcloud-service-key.json
gcloud auth activate-service-account --key-file ${HOME}/gcloud-service-key.json

gcloud --quiet config set project $PROJECT_NAME
gcloud --quiet config set container/cluster $CLUSTER_NAME
gcloud --quiet config set compute/zone ${CLOUDSDK_COMPUTE_ZONE}
gcloud --quiet container clusters get-credentials $CLUSTER_NAME

kubectl config view
kubectl config current-context

kubectl scale --replicas=0 deployment ${KUBE_DEPLOYMENT_NAME} -n e-store
kubectl scale --replicas=1 deployment ${KUBE_DEPLOYMENT_NAME} -n e-store